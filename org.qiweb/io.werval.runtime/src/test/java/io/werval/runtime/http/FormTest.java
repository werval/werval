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
package io.werval.runtime.http;

import io.werval.api.http.FormAttributes;
import io.werval.api.http.FormUploads;
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import java.util.Arrays;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.given;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.request;
import static io.werval.api.mime.MimeTypesNames.TEXT_PLAIN;
import static io.werval.util.Charsets.UTF_8;
import static io.werval.util.InputStreams.BUF_SIZE_16K;
import static org.hamcrest.Matchers.equalTo;

/**
 * Form Test.
 */
public class FormTest
{
    public static class Controller
    {
        public Outcome attributes()
        {
            FormAttributes form = request().body().formAttributes();
            return outcomes().ok( form.allValues().toString() ).asTextual( TEXT_PLAIN ).build();
        }

        public Outcome uploads()
        {
            FormUploads uploads = request().body().formUploads();
            return outcomes().ok( uploads.allValues().toString() ).asTextual( TEXT_PLAIN ).build();
        }
    }

    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "POST /attributes io.werval.runtime.http.FormTest$Controller.attributes\n"
        + "POST /uploads io.werval.runtime.http.FormTest$Controller.uploads"
    ) );

    @Test
    public void attributes()
    {
        given()
            .formParam( "foo", "bar" )
            .formParam( "bazar", "cathedral" )
            .expect()
            .body( equalTo( "{bazar=[cathedral], foo=[bar]}" ) )
            .when()
            .post( "/attributes" );

        given()
            .formParam( "mou", "zou" )
            .formParam( "grou", "mlou" )
            .expect()
            .body( equalTo( "{grou=[mlou], mou=[zou]}" ) )
            .when()
            .post( "/attributes" );

        given()
            .formParam( "foo", "bar" )
            .formParam( "bazar", "cathedral" )
            .expect()
            .body( equalTo( "{bazar=[cathedral], foo=[bar]}" ) )
            .when()
            .post( "/attributes" );
    }

    @Test
    public void smallUpload()
    {
        byte[] upload = "Small upload content".getBytes();
        given()
            .multiPart( "small-upload", "filename.txt", upload, TEXT_PLAIN )
            .expect()
            .body(
                equalTo(
                    "{small-upload=[{contentType: text/plain, charset: UTF-8, filename: filename.txt, length: "
                    + upload.length + " }]}"
                )
            )
            .when()
            .post( "/uploads" );
    }

    @Test
    public void bigUpload()
    {
        byte[] upload = new byte[ BUF_SIZE_16K ];
        Arrays.fill( upload, "A".getBytes( UTF_8 )[0] );
        given()
            .multiPart( "big-upload", "filename.txt", upload, TEXT_PLAIN )
            .expect()
            .body(
                equalTo(
                    "{big-upload=[{contentType: text/plain, charset: UTF-8, filename: filename.txt, length: "
                    + upload.length + " }]}"
                )
            )
            .when()
            .post( "/uploads" );

    }
}
