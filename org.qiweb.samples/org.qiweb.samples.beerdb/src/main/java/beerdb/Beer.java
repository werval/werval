/**
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table( name = "beers" )
public class Beer
{

    public static Beer newBeer( Brewery brewery, String name, float abv, String description )
    {
        Beer beer = new Beer();
        beer.brewery = brewery;
        beer.name = name;
        beer.abv = abv;
        beer.description = description;
        return beer;
    }
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;
    @Column( length = 255, unique = true, nullable = false )
    private String name;
    @Column( length = 4096, nullable = true )
    private String description;
    @Column( nullable = false )
    private float abv;
    @ManyToOne( optional = false )
    private Brewery brewery;

    /**
     * @return The ID of the beer.
     */
    public Long getId()
    {
        return id;
    }

    /**
     * @return The name of the beer.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return The description of the beer.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return The alcohol by volume of the beer.
     */
    public float getAbv()
    {
        return abv;
    }
}
