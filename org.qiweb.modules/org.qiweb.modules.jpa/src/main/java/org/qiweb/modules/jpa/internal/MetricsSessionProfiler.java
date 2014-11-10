/*
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.modules.jpa.internal;

import com.codahale.metrics.Timer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.qiweb.modules.metrics.Metrics;

import static org.qiweb.util.Strings.EMPTY;

/**
 * EclipseLink SessionProfiler that creates Performance Metrics.
 */
/* package */ class MetricsSessionProfiler
    implements SessionProfiler
{
    private static final String METER_PREFIX = "com.eclipse.persistence.meters.";
    private static final String TIMER_PREFIX = "com.eclipse.persistence.timers.";
    private static final String HITSTOGRAM_PREFIX = "com.eclipse.persistence.histograms.";
    private static final String ECLIPSE_COUNTER_PREFIX = "Counter:";
    private static final String ECLIPSE_TIMER_PREFIX = "Timer:";
    private static final String ECLIPSE_INFO_PREFIX = "Info:";

    private final Map<Integer, Map<String, Timer.Context>> operationTimers = new ConcurrentHashMap<>();
    private final Metrics metrics;
    protected int profileWeight;
    protected transient AbstractSession session;

    public MetricsSessionProfiler( Metrics metrics )
    {
        this.profileWeight = SessionProfiler.ALL;
        this.metrics = metrics;
    }

    @Override
    public void initialize()
    {
    }

    @Override
    public void setSession( org.eclipse.persistence.sessions.Session session )
    {
        this.session = (AbstractSession) session;
    }

    @Override
    public void setProfileWeight( int profileWeight )
    {
        this.profileWeight = profileWeight;
    }

    @Override
    public int getProfileWeight()
    {
        return profileWeight;
    }

    @Override
    public Object profileExecutionOfQuery( DatabaseQuery query, Record row, AbstractSession session )
    {
        if( this.profileWeight < SessionProfiler.HEAVY )
        {
            return session.internalExecuteQuery( query, (AbstractRecord) row );
        }
        startOperationProfile( query.getMonitorName() );
        startOperationProfile( query.getClass().getSimpleName() );
        occurred( query.getClass().getSimpleName(), session );
        occurred( query.getMonitorName(), session );
        try
        {
            return session.internalExecuteQuery( query, (AbstractRecord) row );
        }
        finally
        {
            endOperationProfile( query.getMonitorName() );
            endOperationProfile( query.getClass().getSimpleName() );
        }
    }

    @Override
    public void occurred( String operationName, DatabaseQuery query, AbstractSession session )
    {
        if( this.profileWeight < SessionProfiler.NORMAL )
        {
            return;
        }
        occurred( operationName, session );
        occurred( query.getMonitorName() + ":" + operationName, session );
    }

    @Override
    public void occurred( String operationName, AbstractSession session )
    {
        if( this.profileWeight < SessionProfiler.NORMAL )
        {
            return;
        }
        String meterName = METER_PREFIX + operationName.replaceAll( ECLIPSE_COUNTER_PREFIX, EMPTY );
        metrics.metrics().meter( meterName ).mark();
    }

    @Override
    public void startOperationProfile( String operationName, DatabaseQuery query, int weight )
    {
        if( this.profileWeight < weight )
        {
            return;
        }
        startOperationProfile( operationName );
        if( query != null )
        {
            startOperationProfile( query.getMonitorName() + ":" + operationName );
        }
    }

    @Override
    public void startOperationProfile( String operationName )
    {
        String timerName = TIMER_PREFIX + operationName.replaceAll( ECLIPSE_TIMER_PREFIX, EMPTY );
        Timer.Context timer = metrics.metrics().timer( timerName ).time();
        getOperationTimers().put( timerName, timer );
    }

    @Override
    public void endOperationProfile( String operationName, DatabaseQuery query, int weight )
    {
        if( this.profileWeight < weight )
        {
            return;
        }
        endOperationProfile( operationName );
        if( query != null )
        {
            endOperationProfile( query.getMonitorName() + ":" + operationName );
        }
    }

    @Override
    public void endOperationProfile( String operationName )
    {
        if( this.profileWeight < SessionProfiler.HEAVY )
        {
            return;
        }
        String timerName = TIMER_PREFIX + operationName.replaceAll( ECLIPSE_TIMER_PREFIX, EMPTY );
        Timer.Context timer = getOperationTimers().get( timerName );
        if( timer != null )
        {
            timer.close();
        }
    }

    @Override
    public void update( String operationName, Object value )
    {
        if( value instanceof Number )
        {
            String histoName = HITSTOGRAM_PREFIX + operationName.replaceAll( ECLIPSE_INFO_PREFIX, EMPTY );
            metrics.metrics().histogram( histoName ).update( ( (Number) value ).longValue() );
        }
    }

    private Map<String, Timer.Context> getOperationTimers()
    {
        Integer threadId = Thread.currentThread().hashCode();
        Map<String, Timer.Context> timers = operationTimers.get( threadId );
        if( timers == null )
        {
            timers = new HashMap<>();
            operationTimers.put( threadId, timers );
        }
        return timers;
    }
}
