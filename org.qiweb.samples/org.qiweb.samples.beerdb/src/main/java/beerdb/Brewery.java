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
import javax.persistence.Table;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@Entity
@Table( name = "breweries" )
public class Brewery
{

    public static Brewery newBrewery( String name, String url, String description )
    {
        Brewery brewery = new Brewery();
        brewery.name = name;
        brewery.url = url;
        brewery.description = description;
        return brewery;
    }
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;
    @Column( length = 255, unique = true, nullable = false )
    @Length( min = 3, max = 255 )
    private String name;
    @Column( length = 1024, nullable = true )
    @URL
    @Length( max = 1024 )
    private String url;
    @Column
    private Integer since = 1664;
    @Column( length = 16384, nullable = true )
    @Length( max = 16384 )
    private String description;

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public Integer getSince()
    {
        return since;
    }

    public void setSince( Integer since )
    {
        this.since = since;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }
}
