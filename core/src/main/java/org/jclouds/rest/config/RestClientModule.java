/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.rest.config;

import static com.google.common.base.Preconditions.checkState;
import static org.jclouds.rest.config.BinderUtils.bindHttpApi;

import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.jclouds.rest.ConfiguresRestClient;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

/**
 * 
 * @author Adrian Cole
 */
@ConfiguresRestClient
public class RestClientModule<S, A> extends RestModule {
   protected final TypeToken<S> syncClientType;
   protected final TypeToken<A> asyncClientType;

   /**
    * Note that this ctor requires that you instantiate w/resolved generic params. For example, via
    * a subclass of a bound type, or natural instantiation w/resolved type params.
    */
   protected RestClientModule(Map<Class<?>, Class<?>> sync2Async) {
      super(sync2Async);
      this.syncClientType = checkBound(new TypeToken<S>(getClass()) {
         private static final long serialVersionUID = 1L;
      });
      this.asyncClientType = checkBound(new TypeToken<A>(getClass()) {
         private static final long serialVersionUID = 1L;
      });
   }
   
   /**
    * @throws IllegalStateException if the type is an instanceof {@link TypeVariable}
    */
   private static <T> TypeToken<T> checkBound(TypeToken<T> type) throws IllegalStateException {
      checkState(!(type.getType() instanceof TypeVariable<?>),
               "unbound type variable: %s, use ctor that explicitly assigns this", type);
      return type;
   }

   /**
    * @see #RestClientModule(Map)
    */
   protected RestClientModule() {
      this(ImmutableMap.<Class<?>, Class<?>> of());
   }

   /**
    * @see #RestClientModule(TypeToken, TypeToken, Map)
    */
   public RestClientModule(TypeToken<S> syncClientType, TypeToken<A> asyncClientType) {
      this(syncClientType, asyncClientType, ImmutableMap.<Class<?>, Class<?>> of());
   }

   /**
    * only necessary when type params are not resolvable at runtime.
    */
   public RestClientModule(TypeToken<S> syncClientType, TypeToken<A> asyncClientType, Map<Class<?>, Class<?>> sync2Async) {
      super(sync2Async);
      this.syncClientType = checkBound(syncClientType);
      this.asyncClientType = checkBound(asyncClientType);
   }

   @Override
   protected void configure() {
      super.configure();
      bindHttpApi(binder(), syncClientType.getRawType(), asyncClientType.getRawType());
      bindErrorHandlers();
      bindRetryHandlers();
   }

   /**
    * overrides this to change the default retry handlers for the http engine
    * 
    * ex.
    * 
    * <pre>
    * bind(HttpRetryHandler.class).annotatedWith(Redirection.class).to(AWSRedirectionRetryHandler.class);
    * bind(HttpRetryHandler.class).annotatedWith(ClientError.class).to(AWSClientErrorRetryHandler.class);
    * </pre>
    * 
    */
   protected void bindRetryHandlers() {
   }

   /**
    * overrides this to change the default error handlers for the http engine
    * 
    * ex.
    * 
    * <pre>
    * bind(HttpErrorHandler.class).annotatedWith(Redirection.class).to(ParseAWSErrorFromXmlContent.class);
    * bind(HttpErrorHandler.class).annotatedWith(ClientError.class).to(ParseAWSErrorFromXmlContent.class);
    * bind(HttpErrorHandler.class).annotatedWith(ServerError.class).to(ParseAWSErrorFromXmlContent.class);
    * </pre>
    * 
    * 
    */
   protected void bindErrorHandlers() {

   }

}
