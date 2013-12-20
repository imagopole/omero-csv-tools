/**
 *
 */
package org.imagopole.support.unitils.dbunit.datasetloadstrategy.impl;

import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitils.dbunit.datasetloadstrategy.impl.BaseDataSetLoadStrategy;
import org.unitils.dbunit.datasetloadstrategy.impl.RefreshLoadStrategy;
import org.unitils.dbunit.util.DbUnitDatabaseConnection;

/**
 * {@link org.unitils.dbunit.datasetloadstrategy.DataSetLoadStrategy} that both 'unloads'
 * the contents of the database with the contents of the dataset.
 *
 * Also, all data present in the dataset will be deleted upon completion, whether they were present in the
 * database prior to the refresh or they were inserted.
 *
 * @author seb
 *
 * @see RefreshLoadStrategy
 * @see DatabaseOperation#REFRESH
 * @see DatabaseOperation#DELETE
 */
public class UnLoadStrategy extends BaseDataSetLoadStrategy {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(UnLoadStrategy.class);

    /**
     * Executes this DataSetLoadStrategy.
     *
     * This means the given dataset is *deleted* in the database using the given dbUnit
     * database connection object.
     *
     * @param dbUnitDatabaseConnection DbUnit class providing access to the database
     * @param dataSet                  The dbunit dataset
     */
    @Override
    public void doExecute(DbUnitDatabaseConnection dbUnitDatabaseConnection, IDataSet dataSet)
                    throws DatabaseUnitException, SQLException {

        log.debug("Performing delete on dataset: {}", dataSet);

        DatabaseOperation.DELETE.execute(dbUnitDatabaseConnection, dataSet);
    }

}
