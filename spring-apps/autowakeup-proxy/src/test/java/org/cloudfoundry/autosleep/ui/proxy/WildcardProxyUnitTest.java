/*
 * Autosleep
 * Copyright (C) 2016 Orange
 * Authors: Benjamin Einaudi   benjamin.einaudi@orange.com
 *          Arnaud Ruffin      arnaud.ruffin@orange.com
 *
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

package org.cloudfoundry.autosleep.ui.proxy;

import org.cloudfoundry.autosleep.access.cloudfoundry.CloudFoundryApiService;
import org.cloudfoundry.autosleep.access.dao.model.ProxyMapEntry;
import org.cloudfoundry.autosleep.access.dao.repositories.ProxyMapEntryRepository;
import org.cloudfoundry.autosleep.config.Config.CloudFoundryAppState;
import org.cloudfoundry.autosleep.util.TimeManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WildcardProxyUnitTest.Configuration.class)
public class WildcardProxyUnitTest {

    @org.springframework.context.annotation.Configuration
    @ComponentScan(basePackages = {"org.cloudfoundry.autosleep.ui.proxy"},
            includeFilters = @Filter(Controller.class),
            excludeFilters = @Filter(org.springframework.context.annotation.Configuration.class))
    public static class Configuration {

        @Bean
        CloudFoundryApiService cfApi() {
            return mock(CloudFoundryApiService.class);
        }

        @Bean
        ProxyMapEntryRepository proxyMap() {
            return mock(ProxyMapEntryRepository.class);
        }

        @Bean
        RestTemplate restTemplate() {
            return mock(RestTemplate.class);
        }

        @Bean
        TimeManager timeManager() {
            return mock(TimeManager.class);
        }

    }

    private static final String APP_ID = "test-app-id";
    private static final String APP_ID2 = "other-test-app-id";

    private static final String HOST_TEST_VALUE = "test-host";

    @Autowired
    private CloudFoundryApiService cfApi;

    @Autowired
    private WildcardProxy proxy;

    @Autowired
    private ProxyMapEntryRepository proxyMap;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TimeManager timeManager;

    @Before
    public void init() {
        reset(timeManager, proxyMap, cfApi, restTemplate);
    }

    @Test
    public void first_received_query_should_start_sleeping_apps() throws Exception {
        //given 2 registered sleeping apps
        List<ProxyMapEntry> proxyMapEntries = asList(
                ProxyMapEntry.builder()
                        .appId(APP_ID2)
                        .host(HOST_TEST_VALUE)
                        .build(),
                ProxyMapEntry.builder()
                        .appId(APP_ID)
                        .host(HOST_TEST_VALUE)
                        .build()
        );
        when(proxyMap.findByHost(HOST_TEST_VALUE)).thenReturn(proxyMapEntries);
        //and the 1st app is starting fast
        when(cfApi.getApplicationState(APP_ID)).thenReturn(
                CloudFoundryAppState.STOPPED,
                CloudFoundryAppState.STARTED);
        when(cfApi.isAppRunning(APP_ID)).thenReturn(
                false,
                true);
        //while the 2nd is slower to start
        when(cfApi.getApplicationState(APP_ID2)).thenReturn(
                CloudFoundryAppState.STOPPED,
                CloudFoundryAppState.STARTED);
        when(cfApi.isAppRunning(APP_ID2)).thenReturn(
                false,
                false,
                false,
                true);

        //when
        boolean appIsReadyToTakeTraffic = proxy.startSleepingAppsIfAny(proxyMapEntries);

        //then
        assertThat(appIsReadyToTakeTraffic, is(true));
        // and start was not called
        verify(cfApi).startApplication(APP_ID);
        verify(cfApi).startApplication(APP_ID2);
    }

}
