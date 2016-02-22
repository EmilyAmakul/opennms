/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CategoriesBoxTest extends OpenNMSSeleniumTestCase {

    @Before
    public void before() {
        m_driver.get(BASE_URL+"opennms");
    }

    /**
     * This test typically runs first, so we opt-in here.
     *
     * FIXME: This logic must be moved into the default OpenNMSSeleniumTestCase
     * implementation (or alternative) before being merge upstream
     */
    @Test
    public void canEnableDatachoices() throws Exception {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.id("datachoices-enable")));
            findElementById("datachoices-enable").click();
        } catch (Throwable t) {
            // pass
        }
    }

    @Test
    public void testAlarmLink() throws Exception {
        // Hit the default "Network Interfaces" link on the startpage
        findElementByLink("Network Interfaces").click();
        // check for correct url...
        wait.until(ExpectedConditions.urlContains("/opennms/rtc/category.jsp"));
        // ...and header cell
        findElementByXpath("//th[text()='24hr Availability']");
    }
}
