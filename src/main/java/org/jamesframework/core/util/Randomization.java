/*
 * Copyright 2015 Ghent University, Bayer CropScience.
 *
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
 */

package org.jamesframework.core.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomization tools. All randomized components in the framework retrieve the applied
 * random generator through {@link #getRandom()}. By default, a thread local random generator
 * is returned to avoid overhead and contention in parallel searches. If desired, a custom random
 * generator can be set using {@link #setRandom(Random)}.
 * <p>
 * For consistency, it is advised to always use {@link #getRandom()} in application specific code
 * as well, whenever a random generator is used.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class Randomization {
    
    // custom random generator; null if none set
    private static Random RG = null;
    
    /**
     * Set a custom random generator to be used by all randomized components in the framework.
     * The given random generator is stored and can be retrieved with {@link #getRandom()}.
     * If <code>null</code> is given as argument, the default behaviour is reset, so that
     * {@link #getRandom()} returns a thread local random generator.
     * 
     * @param random custom random generator to be used in all randomized components
     */
    public static void setRandom(Random random){
        RG = random;
    }
    
    /**
     * Reset the default behaviour so that {@link #getRandom()} returns a thread local random generator.
     * If a custom random generator had been set before, it is discarded. Calling this method is equivalent
     * to calling {@code setRandom(null)}.
     */
    public static void resetDefault(){
        setRandom(null);
    }
    
    /**
     * Retrieve the random generator set with {@link #setRandom(Random)}. By default, i.e. if no custom
     * random generator has been set, this method returns a thread local random generator as obtained
     * through {@code ThreadLocalRandom.current()} to avoid overhead and contention in parallel searches.
     * 
     * @return custom random generator, if set; else, a thread local random generator
     */
    public static Random getRandom(){
        return RG != null ? RG : ThreadLocalRandom.current();
    }

}
