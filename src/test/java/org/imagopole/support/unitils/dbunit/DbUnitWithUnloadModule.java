/**
 *
 */
package org.imagopole.support.unitils.dbunit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.dbunit.dataset.IDataSet;
import org.imagopole.support.unitils.dbunit.annotation.UnloadDataSet;
import org.imagopole.support.unitils.dbunit.datasetloadstrategy.impl.UnLoadStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitils.core.TestListener;
import org.unitils.core.UnitilsException;
import org.unitils.dbunit.DbUnitModule;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.datasetloadstrategy.DataSetLoadStrategy;
import org.unitils.dbunit.util.MultiSchemaDataSet;

/**
 * @author seb
 *
 */
public class DbUnitWithUnloadModule extends DbUnitModule {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DbUnitWithUnloadModule.class);

    /**
     *
     * @see org.unitils.dbunit.DbUnitModule#getTestListener()
     */
    @Override
    public TestListener getTestListener() {
        return new DbUnitTearDownListener();
    }

    /**
     * This method will first try to load a method level defined dataset. If no such file exists, a class level defined
     * dataset will be loaded. If neither of these files exist, nothing is done.
     * The name of the test data file at both method level and class level can be overridden using the
     * {@link DataSet} annotation. If specified using this annotation but not found, a {@link UnitilsException} is
     * thrown.
     *
     * @param testMethod The method, not null
     * @param testObject The test object, not null
     */
    public void deleteDataSet(Method testMethod, Object testObject) {
        try {
            MultiSchemaDataSet multiSchemaDataSet = getDataSet(testMethod, testObject);
            if (multiSchemaDataSet == null) {
                log.debug("No schema specified - no-op");
                // no dataset specified
                return;
            }
            //DataSetLoadStrategy dataSetLoadStrategy = getDataSetLoadStrategy(testMethod, testObject.getClass());
            boolean shouldUnload = shouldUnloadDataset(testMethod, testObject);

            if (shouldUnload) {
                DataSetLoadStrategy dataSetLoadStrategy = new UnLoadStrategy();

                log.debug("Executing unload strategy: {} on {}#{}",
                           dataSetLoadStrategy.getClass().getSimpleName(),
                           testObject.getClass().getSimpleName(), testMethod.getName());

                deleteDataSet(multiSchemaDataSet, dataSetLoadStrategy);
            }

        } catch (Exception e) {
            throw new UnitilsException("Error deleting test data from DbUnit dataset for method " + testMethod, e);
        } finally {
            closeJdbcConnection();
        }
    }

    protected boolean shouldUnloadDataset(Method testMethod, Object testObject) {
        Annotation annotation = testMethod.getAnnotation(UnloadDataSet.class);

        boolean shouldUnload = (null != annotation);
        log.debug("shouldUnload {}#{}? {}",
                   testObject.getClass().getSimpleName(), testMethod.getName(), shouldUnload);

        return shouldUnload;
    }

    /**
     * Loads the given multi schema dataset into the database, using the given loadstrategy
     *
     * @param multiSchemaDataSet  The multi schema dataset that is inserted in the database
     * @param dataSetLoadStrategy The load strategy that is used
     */
    protected void deleteDataSet(MultiSchemaDataSet multiSchemaDataSet, DataSetLoadStrategy dataSetLoadStrategy) {

        try {
            for (String schemaName : multiSchemaDataSet.getSchemaNames()) {
                IDataSet schemaDataSet = multiSchemaDataSet.getDataSetForSchema(schemaName);
                log.trace("Deleting dataset {} ", schemaDataSet);

                dataSetLoadStrategy.execute(getDbUnitDatabaseConnection(schemaName), schemaDataSet);
            }
        } finally {
            closeJdbcConnection();
        }
    }

    /**
     * Test listener that is called while the test framework is running tests
     */
    protected class DbUnitTearDownListener extends DbUnitListener {

        @Override
        public void beforeTestSetUp(Object testObject, Method testMethod) {
            log.debug("Setting up {}#{} ", testObject.getClass().getSimpleName(), testMethod.getName());

            insertDataSet(testMethod, testObject);
        }

        @Override
        public void afterTestMethod(Object testObject, Method testMethod, Throwable throwable) {
            log.debug("Checking assert {}#{} ", testObject.getClass().getSimpleName(), testMethod.getName());

            if (throwable == null) {
                assertDbContentAsExpected(testMethod, testObject);
            }
        }

        /**
         * @see org.unitils.core.TestListener#afterTestTearDown(java.lang.Object, java.lang.reflect.Method)
         */
        @Override
        public void afterTestTearDown(Object testObject, Method testMethod) {
            boolean shouldUnload = shouldUnloadDataset(testMethod, testObject);
            if (shouldUnload) {
                log.debug("Tearing down {}#{} ",
                          testObject.getClass().getSimpleName(), testMethod.getName());

                deleteDataSet(testMethod, testObject);
            }
        }

    }

}
