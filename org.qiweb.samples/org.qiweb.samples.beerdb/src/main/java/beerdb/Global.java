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

import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.standard.StandardRepresentationFactory;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.qiweb.api.Application;

public class Global
    extends org.qiweb.api.Global
{

    @Override
    public void onStart( Application application )
    {
        // Persistence
        String persistenceUnitName = application.config().string( "app.persistence-unit-name" );
        EntityManagerFactory emf = Persistence.createEntityManagerFactory( persistenceUnitName );
        application.metaData().put( "emf", emf );

        // Hypertext Application Language
        RepresentationFactory halFactory = new StandardRepresentationFactory();
        halFactory.withFlag( RepresentationFactory.PRETTY_PRINT );
        application.metaData().put( "hal", halFactory );
    }

    @Override
    public void onStop( Application application )
    {
        // Persistence
        EntityManagerFactory emf = application.metaData().get( EntityManagerFactory.class, "emf" );
        application.metaData().remove( "emf" );
        emf.close();

        // Hypertext Application Language
        application.metaData().remove( "hal" );
    }
}
