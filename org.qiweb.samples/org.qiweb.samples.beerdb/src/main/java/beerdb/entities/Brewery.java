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
package beerdb.entities;

import beerdb.Json;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

@Entity
@Table( name = "breweries" )
@JsonIgnoreProperties( ignoreUnknown = true )
public class Brewery
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;
    @Column( length = 255, nullable = false )
    @NotBlank
    @Length( min = 3, max = 255 )
    private String name;
    @Column
    @NotNull
    @Range( min = 0 )
    private Integer since = 1664;
    @Column( length = 1024, nullable = true )
    @URL
    @NotBlank
    @Length( max = 1024 )
    private String url;
    @Column( length = 16384, nullable = true )
    @Length( max = 16384 )
    private String description;
    @Column( name = "beers_count", nullable = false )
    /* package */ Integer beersCount = 0;
    @OneToMany( mappedBy = "brewery" )
    private List<Beer> beers = new ArrayList<>();

    @JsonView(
         {
            Json.BreweryListView.class, Json.BreweryDetailView.class,
            Json.BeerListView.class, Json.BeerDetailView.class
        } )
    public Long getId()
    {
        return id;
    }

    @JsonView(
         {
            Json.BreweryListView.class, Json.BreweryDetailView.class,
            Json.BeerListView.class, Json.BeerDetailView.class
        } )
    public String getName()
    {
        return name;
    }

    @JsonView( Json.BreweryDetailView.class )
    public String getUrl()
    {
        return url;
    }

    @JsonView(
         {
            Json.BreweryListView.class, Json.BreweryDetailView.class
        } )
    public Integer getSince()
    {
        return since;
    }

    @JsonView( Json.BreweryDetailView.class )
    public String getDescription()
    {
        return description;
    }

    @JsonView( Json.BreweryDetailView.class )
    public List<Beer> getBeers()
    {
        return beers;
    }

    @JsonView( Json.BreweryListView.class )
    public Integer getBeersCount()
    {
        return beersCount;
    }

    @JsonDeserialize
    public void setName( String name )
    {
        this.name = name == null ? name : name.trim();
    }

    public void setSince( Integer since )
    {
        this.since = since;
    }

    @JsonDeserialize
    public void setUrl( String url )
    {
        this.url = url == null ? null : url.trim();
    }

    @JsonDeserialize
    public void setDescription( String description )
    {
        this.description = description == null ? null : description.trim();
    }

    public void addBeer( Beer beer )
    {
        beers.add( beer );
        beer.brewery = this;
        beersCount += 1;
    }

    public void removeBeer( Beer beer )
    {
        beers.remove( beer );
        beer.brewery = null;
        beersCount -= 1;
    }
}
