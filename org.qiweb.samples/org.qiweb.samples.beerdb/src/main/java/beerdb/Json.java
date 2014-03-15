/*
 * Copyright (c) 2013 the original author or authors
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
package beerdb;

/**
 * Jackson JSON Related utilities.
 *
 * <ul>
 * <li><a href="http://wiki.fasterxml.com/JacksonJsonViews" target="_blank">Jackson JSON Views</a> class IDs</li>
 * </ul>
 * <p>
 * Views are declared here, used in Entities through Jackson annotations and used in the HTTP API to produce
 * resources representations.
 */
public interface Json
{
    interface BeerListView
    {
    }

    interface BeerDetailView
    {
    }

    interface BreweryListView
    {
    }

    interface BreweryDetailView
    {
    }
}
