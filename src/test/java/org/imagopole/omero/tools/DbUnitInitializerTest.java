package org.imagopole.omero.tools;

import org.imagopole.omero.tools.TestsUtil.DbUnit.DataSets;
import org.imagopole.omero.tools.TestsUtil.Groups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.datasetloadstrategy.impl.RefreshLoadStrategy;

/**
 * Workaround required to initialize the database to a state usable for authentication
 * from within integration tests (ie. with experimenter + password and group).
 *
 * To do this, <code>initDatabaseState</code> must be run at least once before any
 * <code>AbstractBlitzClientConnectorTest</code> is <code>setUp</code> (which requires credentials
 * for the OMERO client initialization).
 *
 * Current workaround: first run this once to init db, then run full integration suite.
 *
 * @author seb
 *
 */
public class DbUnitInitializerTest extends UnitilsTestNG {

    /** Application logs */
    private final Logger log = LoggerFactory.getLogger(DbUnitInitializerTest.class);

    @Test(groups = { Groups.SETUP }, alwaysRun = true)
    @BeforeGroups(groups = { Groups.INTEGRATION })
    @DataSet(value = DataSets.Xml.COMMON, loadStrategy = RefreshLoadStrategy.class)
    public void initDatabaseState() {
        // let DbUnit populate or refresh the base fixures (experimenter, group, event, session, etc).
        log.debug("Performing database state refresh");
    }

}
