/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.memory;

import io.trino.testing.AbstractTestQueryFramework;
import io.trino.testing.QueryRunner;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMemoryTableStatistics
        extends AbstractTestQueryFramework
{
    @Override
    protected QueryRunner createQueryRunner()
            throws Exception
    {
        return MemoryQueryRunner.createQueryRunner();
    }

    @Test
    public void testBasic()
    {
        assertQuery(
                "SHOW STATS FOR nation",
                "VALUES " +
                        "('nationkey', null, null, null, null, null, null)," +
                        "('name', null, null, null, null, null, null)," +
                        "('regionkey', null, null, null, null, null, null)," +
                        "('comment', null, null, null, null, null, null)," +
                        "(null, null, null, null, 25, null, null)");
    }

    @Test
    public void testEmptyTable()
    {
        computeActual("CREATE TABLE empty_table AS TABLE nation WITH NO DATA");
        assertQuery(
                "SHOW STATS FOR empty_table",
                "VALUES " +
                        "('nationkey', 0, 0, 1, null, null, null)," +
                        "('name', 0, 0, 1, null, null, null)," +
                        "('regionkey', 0, 0, 1, null, null, null)," +
                        "('comment', 0, 0, 1, null, null, null)," +
                        "(null, null, null, null, 0, null, null)");
    }

    @Test
    public void testView()
    {
        computeActual("CREATE VIEW nation_view AS TABLE nation");
        assertQuery(
                "SHOW STATS FOR nation_view",
                "VALUES " +
                        "('nationkey', null, null, null, null, null, null)," +
                        "('name', null, null, null, null, null, null)," +
                        "('regionkey', null, null, null, null, null, null)," +
                        "('comment', null, null, null, null, null, null)," +
                        "(null, null, null, null, 25.0, null, null)");
    }

    @Test
    public void testStatsWithLimitPushdown()
    {
        String query = "SELECT * FROM nation LIMIT 3";
        assertThat(query(query)).skipResultsCorrectnessCheckForPushdown().isFullyPushedDown();

        assertQuery(
                "SHOW STATS FOR (" + query + ")",
                "VALUES " +
                        "('nationkey', null, null, null, null, null, null)," +
                        "('name', null, null, null, null, null, null)," +
                        "('regionkey', null, null, null, null, null, null)," +
                        "('comment', null, null, null, null, null, null)," +
                        "(null, null, null, null, 25, null, null)"); // TODO should be 3
    }

    @Test
    public void testStatsWithSamplePushdown()
    {
        String query = "SELECT * FROM nation TABLESAMPLE SYSTEM (50)";
        assertThat(query(query)).skipResultsCorrectnessCheckForPushdown().isFullyPushedDown();

        assertQuery(
                "SHOW STATS FOR (" + query + ")",
                "VALUES " +
                        "('nationkey', null, null, null, null, null, null)," +
                        "('name', null, null, null, null, null, null)," +
                        "('regionkey', null, null, null, null, null, null)," +
                        "('comment', null, null, null, null, null, null)," +
                        "(null, null, null, null, 25, null, null)"); // TODO should be 12.5
    }
}
