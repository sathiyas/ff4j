package org.ff4j.test.strategy;

import java.text.ParseException;
import java.util.Date;

import org.junit.Assert;

import org.ff4j.FF4j;
import org.ff4j.core.Feature;
import org.ff4j.strategy.ReleaseDateFlipStrategy;
import org.ff4j.test.AbstractFf4jTest;
import org.junit.Test;

/*
 * #%L
 * ff4j-core
 * %%
 * Copyright (C) 2013 Ff4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Testing class for {@link ReleaseDateFlipStrategy} class.
 * 
 * @author <a href="mailto:cedrick.lunven@gmail.com">Cedrick LUNVEN</a>
 */
public class ReleaseDateFlipStrategyTest extends AbstractFf4jTest {

    /** {@inheritDoc} */
    @Override
    public FF4j initFF4j() {
        return new FF4j("test-releaseDateStrategyTest-ok.xml");
    }

    @Test
    public void testPastDayOK() throws ParseException {
        Feature f = ff4j.getFeature("past1");
        ReleaseDateFlipStrategy rds = (ReleaseDateFlipStrategy) f.getFlippingStrategy();
        Assert.assertTrue(new Date().after(rds.getReleaseDate()));
    }

    @Test
    public void testFutureOK() throws ParseException {
        Feature f = ff4j.getFeature("future1");
        ReleaseDateFlipStrategy rds = (ReleaseDateFlipStrategy) f.getFlippingStrategy();
        Assert.assertTrue(new Date().before(rds.getReleaseDate()));
    }

}
