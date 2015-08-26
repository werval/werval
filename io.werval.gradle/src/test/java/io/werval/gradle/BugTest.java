package io.werval.gradle;

import org.junit.Ignore;
import org.junit.Test;

public class BugTest
{
    @Test
    @Ignore
    public void test()
        throws Exception
    {
        String line = "java -cp /Users/paul/src/werval-related/werval/io.werval.gradle/build/tmp/it/junit7626232014264831595/build/classes/main/:/Users/paul/src/werval-related/werval/io.werval.gradle/build/tmp/it/junit7626232014264831595/build/resources/main/:/Users/paul/src/werval-related/werval/io.werval.gradle/build/tmp/it/junit7626232014264831595/build/classes/custom:/Users/paul/src/werval-related/werval/io.werval.gradle/build/tmp/it/junit7626232014264831595/build/resources/custom/:/Users/paul/src/werval-related/werval/repository/io/werval/io.werval.api/0/io.werval.api-0.jar:/Users/paul/src/werval-related/werval/repository/io/werval/io.werval.server.bootstrap/0/io.werval.server.bootstrap-0.jar:/Users/paul/.gradle/caches/modules-2/files-2.1/ch.qos.logback/logback-classic/1.1.2/b316e9737eea25e9ddd6d88eaeee76878045c6b2/logback-classic-1.1.2.jar:/Users/paul/.gradle/caches/modules-2/files-2.1/org.slf4j/slf4j-api/1.7.12/8e20852d05222dc286bf1c71d78d0531e177c317/slf4j-api-1.7.12.jar:/Users/paul/src/werval-related/werval/repository/io/werval/io.werval.spi/0/io.werval.spi-0.jar:/Users/paul/src/werval-related/werval/repository/io/werval/io.werval.server.netty/0/io.werval.server.netty-0.jar:/Users/paul/.gradle/caches/modules-2/files-2.1/ch.qos.logback/logback-core/1.1.2/2d23694879c2c12f125dac5076bdfd5d771cc4cb/logback-core-1.1.2.jar:/Users/paul/src/werval-related/werval/repository/io/werval/io.werval.runtime/0/io.werval.runtime-0.jar:/Users/paul/.gradle/caches/modules-2/files-2.1/io.netty/netty-codec-http/4.0.28.Final/9c66eb5e363caa6a73516f83ebb2bfe24c0b32b8/netty-codec-http-4.0.28.Final.jar:/Users/paul/.gradle/caches/modules-2/files-2.1/org.javassist/javassist/3.19.0-GA/50120f69224dd8684b445a6f3a5b08fe9b5c60f6/javassist-3.19.0-GA.jar:/Users/paul/.gradle/caches/modules-2/files-2.1/com.typesafe/config/1.3.0/f533aa6ea13e443b50e639d070986c42d03efc35/config-1.3.0.jar:/Users/paul/.gradle/caches/modules-2/files-2.1/io.netty/netty-codec/4.0.28.Final/9b68eeff7f7f7868a1e56a60fdfc96211b38bb08/netty-codec-4.0.28.Final.jar:/Users/paul/.gradle/caches/modules-2/files-2.1/io.netty/netty-handler/4.0.28.Final/355466e03aee91961bd316a8bc32759a6e9f0c83/netty-handler-4.0.28.Final.jar:/Users/paul/.gradle/caches/modules-2/files-2.1/io.netty/netty-transport/4.0.28.Final/cce15e674bd1b51f81be1394baaa170b73b62b9b/netty-transport-4.0.28.Final.jar:/Users/paul/.gradle/caches/modules-2/files-2.1/io.netty/netty-buffer/4.0.28.Final/f8bfa26e51e9701042c35a3135b57d2c0a770165/netty-buffer-4.0.28.Final.jar:/Users/paul/.gradle/caches/modules-2/files-2.1/io.netty/netty-common/4.0.28.Final/d1f452cd406c3ffa6590af77546393374d9c660b/netty-common-4.0.28.Final.jar -Dconfig.resource=application-custom.conf -Dconfig.trace=loads io.werval.server.bootstrap.Main";
        final Process proc = new ProcessBuilder( line.split( " " ) ).inheritIO().start();
        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep( 10_000 );
                }
                catch( InterruptedException ex )
                {
                    ex.printStackTrace();
                }
                proc.destroy();
            }
        } ).start();
        int status = proc.waitFor();
        if( status != 0 )
        {
            throw new Exception( "Fork status NOT ZERO, was " + status );
        }
    }
}
