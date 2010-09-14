/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.web.controller.router;

import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.RouteDescriptor;
import org.exoplatform.web.controller.metadata.RouterDescriptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestRequestParam extends AbstractTestController
{

   public void testRoot() throws Exception
   {
      RouterDescriptor descriptor = new RouterDescriptor();
      descriptor.addRoute(new RouteDescriptor("/").addRequestParam(QualifiedName.parse("foo"), "a", "a", true));
      Router router = new Router(descriptor);

      //
      assertNull(router.route("/"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/", Collections.singletonMap("a", new String[]{"a"})));

      //
      assertNull(router.render(Collections.<QualifiedName, String>emptyMap()));
      SimpleRenderContext renderContext = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("foo"), "a"), renderContext);
      assertEquals("/", renderContext.getPath());
      assertEquals(Collections.singletonMap("a", "a"), renderContext.getQueryParams());
   }

   public void testSegment() throws Exception
   {
      RouterDescriptor descriptor = new RouterDescriptor();
      descriptor.addRoute(new RouteDescriptor("/a").addRequestParam(QualifiedName.parse("foo"), "a", "a", true));
      Router router = new Router(descriptor);

      //
      assertNull(router.route("/a"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));

      //
      assertNull(router.render(Collections.<QualifiedName, String>emptyMap()));
      SimpleRenderContext renderContext = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("foo"), "a"), renderContext);
      assertEquals("/a", renderContext.getPath());
      assertEquals(Collections.singletonMap("a", "a"), renderContext.getQueryParams());
   }

   public void testValuePattern() throws Exception
   {
      RouterDescriptor descriptor = new RouterDescriptor();
      descriptor.addRoute(new RouteDescriptor("/a").addRequestParam(QualifiedName.parse("foo"), "a", "{[0-9]+}", true));
      Router router = new Router(descriptor);

      //
      assertNull(router.route("/a"));
      assertNull(router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "0123"), router.route("/a", Collections.singletonMap("a", new String[]{"0123"})));

      //
      assertNull(router.render(Collections.<QualifiedName, String>emptyMap()));
      assertNull(router.render(Collections.singletonMap(QualifiedName.parse("foo"), "a")));
      SimpleRenderContext renderContext = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("foo"), "12"), renderContext);
      assertEquals("/a", renderContext.getPath());
      assertEquals(Collections.singletonMap("a", "12"), renderContext.getQueryParams());
   }

   public void testPrecedence() throws Exception
   {
      RouterDescriptor descriptor = new RouterDescriptor();
      descriptor.addRoute(new RouteDescriptor("/a").addRequestParam(QualifiedName.parse("foo"), "a", "a", true));
      descriptor.addRoute(new RouteDescriptor("/a").addRequestParam(QualifiedName.parse("bar"), "b", "b", true));
      Router router = new Router(descriptor);

      //
      assertNull(router.route("/a"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
      assertEquals(Collections.singletonMap(QualifiedName.parse("bar"), "b"), router.route("/a", Collections.singletonMap("b", new String[]{"b"})));

      //
      assertNull(router.render(Collections.<QualifiedName, String>emptyMap()));
      SimpleRenderContext renderContext1 = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("foo"), "a"), renderContext1);
      assertEquals("/a", renderContext1.getPath());
      assertEquals(Collections.singletonMap("a", "a"), renderContext1.getQueryParams());
      SimpleRenderContext renderContext2 = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("bar"), "b"), renderContext2);
      assertEquals("/a", renderContext2.getPath());
      assertEquals(Collections.singletonMap("b", "b"), renderContext2.getQueryParams());
   }

   public void testInheritance() throws Exception
   {
      RouterDescriptor descriptor = new RouterDescriptor();
      descriptor.addRoute(new RouteDescriptor("/a").addRequestParam(QualifiedName.parse("foo"), "a", "a", true).addChild(new RouteDescriptor("/b").addRequestParam(QualifiedName.parse("bar"), "b", "b", true)));
      Router router = new Router(descriptor);

      //
      assertNull(router.route("/a"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/a", Collections.singletonMap("a", new String[]{"a"})));
      assertNull(router.route("/a/b"));
      Map<String, String[]> requestParameters = new HashMap<String, String[]>();
      requestParameters.put("a", new String[]{"a"});
      requestParameters.put("b", new String[]{"b"});
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.parse("foo"), "a");
      expectedParameters.put(QualifiedName.parse("bar"), "b");
      assertEquals(expectedParameters, router.route("/a/b", requestParameters));

      //
      assertNull(router.render(Collections.<QualifiedName, String>emptyMap()));
      SimpleRenderContext renderContext1 = new SimpleRenderContext();
      router.render(Collections.singletonMap(QualifiedName.parse("foo"), "a"), renderContext1);
      assertEquals("/a", renderContext1.getPath());
      assertEquals(Collections.singletonMap("a", "a"), renderContext1.getQueryParams());

      SimpleRenderContext renderContext2 = new SimpleRenderContext();
      router.render(expectedParameters, renderContext2);
      assertEquals("/a/b", renderContext2.getPath());
      Map<String, String> expectedRequestParameters = new HashMap<String, String>();
      expectedRequestParameters.put("a", "a");
      expectedRequestParameters.put("b", "b");
      assertEquals(expectedRequestParameters, renderContext2.getQueryParams());
   }

   public void testOptional() throws Exception
   {
      RouterDescriptor descriptor = new RouterDescriptor();
      descriptor.addRoute(new RouteDescriptor("/").addRequestParam(QualifiedName.parse("foo"), "a", "a", false));
      Router router = new Router(descriptor);

      //
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/", Collections.<String, String[]>emptyMap()));
      assertEquals(Collections.singletonMap(QualifiedName.parse("foo"), "a"), router.route("/", Collections.singletonMap("a", new String[]{"a"})));
   }

}
