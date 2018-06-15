/*
 * Copyright (c) [2016] [ <tch.camp> ]
 * This file is part of the tcheumJ library.
 *
 * The tcheumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The tcheumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the tcheumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tcheum.facade;

import org.tcheum.config.DefaultConfig;
import org.tcheum.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;


/**
 * @author Roman Mandeleil
 * @since 13.11.2014
 */
@Component
public class tcheumFactory {

    private static final Logger logger = LoggerFactory.getLogger("general");

    public static tcheum createtcheum() {
        return createtcheum((Class) null);
    }

    public static tcheum createtcheum(Class userSpringConfig) {
        return userSpringConfig == null ? createtcheum(new Class[] {DefaultConfig.class}) :
                createtcheum(DefaultConfig.class, userSpringConfig);
    }

    /**
     * @deprecated The config parameter is not used anymore. The configuration is passed
     * via 'systemProperties' bean either from the DefaultConfig or from supplied userSpringConfig
     * @param config  Not used
     * @param userSpringConfig   User Spring configuration class
     * @return  Fully initialized tcheum instance
     */
    public static tcheum createtcheum(SystemProperties config, Class userSpringConfig) {

        return userSpringConfig == null ? createtcheum(new Class[] {DefaultConfig.class}) :
                createtcheum(DefaultConfig.class, userSpringConfig);
    }

    public static tcheum createtcheum(Class ... springConfigs) {
        logger.info("Starting tcheumJ...");
        ApplicationContext context = new AnnotationConfigApplicationContext(springConfigs);

        return context.getBean(tcheum.class);
    }
}
