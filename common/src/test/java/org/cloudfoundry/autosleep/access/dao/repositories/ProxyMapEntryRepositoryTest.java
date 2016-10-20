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

package org.cloudfoundry.autosleep.access.dao.repositories;

import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.autosleep.access.dao.config.RepositoryConfig;
import org.cloudfoundry.autosleep.access.dao.model.ProxyMapEntry;
import org.cloudfoundry.autosleep.util.ApplicationConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfiguration.class, RepositoryConfig.class, EnableJpaConfiguration.class})
public abstract class ProxyMapEntryRepositoryTest extends CrudRepositoryTest<ProxyMapEntry> {

    @Autowired
    private ProxyMapEntryRepository repository;

    @Override
    protected ProxyMapEntry build(String host) {
        return ProxyMapEntry.builder().host(host).appId("appId").build();
    }

    @Override
    protected void compareReloaded(ProxyMapEntry original, ProxyMapEntry reloaded) {
        assertThat(reloaded.getHost(), is(equalTo(original.getHost())));
        assertThat(reloaded.getAppId(), is(equalTo(original.getAppId())));
        assertThat(reloaded, is(equalTo(original)));
    }

    @Before
    @After
    public void setAndClearDao() {
        setDao(repository);
        repository.deleteAll();
    }

    @Test
    public void test_findByHost() {
        //given
        ProxyMapEntryRepository entryRepository = (ProxyMapEntryRepository) getDao();
        ProxyMapEntry a = new ProxyMapEntry("host.cfapp.io", "1");
        ProxyMapEntry b = new ProxyMapEntry("host.cfapp.io", "2");
        ProxyMapEntry c = new ProxyMapEntry("otherhost.cfapp.io", "3");
        entryRepository.save(asList(a, b, c));

        //when
        List<ProxyMapEntry> entries = entryRepository.findByHost("host.cfapp.io");

        //then
        assertThat(entries, is(equalTo(asList(a, b))));
    }

}