/**
 *
 */
package org.imagopole.support.unitils.dbunit.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation indicating that a data set should be unloaded after the test run.
 *
 * Convenient for testing in an environment where database is not re-populated from
 * a clean sheet.
 *
 * @author seb
 *
 */
@Target({METHOD})
@Retention(RUNTIME)
@Inherited
public @interface UnloadDataSet {

}
