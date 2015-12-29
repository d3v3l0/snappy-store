/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.gemstone.gemfire.management.internal.web.shell;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.gemstone.gemfire.internal.lang.StringUtils;
import com.gemstone.gemfire.management.internal.cli.CommandRequest;
import com.gemstone.gemfire.management.internal.web.AbstractWebTestCase;
import com.gemstone.gemfire.management.internal.web.http.ClientHttpRequest;
import com.gemstone.gemfire.management.internal.web.http.HttpMethod;
import com.gemstone.gemfire.management.internal.web.domain.Link;
import com.gemstone.gemfire.management.internal.web.domain.LinkIndex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

/**
 * The RestHttpOperationInvokerJUnitTest class is a test suite of test cases testing the contract and functionality of the
 * RestHttpOperationInvoker class.
 * <p/>
 * @author John Blum
 * @see java.net.URI
 * @see com.gemstone.gemfire.management.internal.cli.CommandRequest
 * @see com.gemstone.gemfire.management.internal.web.http.HttpMethod
 * @see com.gemstone.gemfire.management.internal.web.domain.Link
 * @see com.gemstone.gemfire.management.internal.web.domain.LinkIndex
 * @see com.gemstone.gemfire.management.internal.web.shell.RestHttpOperationInvoker
 * @see org.junit.Assert
 * @see org.junit.Test
 * @since 7.5
 */
@SuppressWarnings("unused")
public class RestHttpOperationInvokerJUnitTest extends AbstractWebTestCase {

  private LinkIndex linkIndex;

  private RestHttpOperationInvoker operationInvoker;

  @Before
  public void setUp() throws Exception {
    final Link listLibraries = new Link("list-libraries", toUri("http://host.domain.com/service/v1/libraries"));
    final Link getLibrary = new Link("get-library", toUri("http://host.domain.com/service/v1/libraries/{name}"));
    final Link listBooks = new Link("list-books", toUri("http://host.domain.com/service/v1/libraries/{name}/books"));
    final Link listBooksByAuthor = new Link("list-books", toUri("http://host.domain.com/service/v1/libraries/{name}/books/{author}"));
    final Link listBooksByAuthorAndCategory = new Link("list-books", toUri("http://host.domain.com/service/v1/libraries/{name}/books/{author}/{category}"));
    final Link listBooksByAuthorAndYear = new Link("list-books", toUri("http://host.domain.com/service/v1/libraries/{name}/books/{author}/{year}"));
    final Link listBooksByAuthorCategoryAndYear = new Link("list-books", toUri("http://host.domain.com/service/v1/libraries/{name}/books/{author}/{category}/{year}"));
    final Link addBook = new Link("add-book", toUri("http://host.domain.com/service/v1/libraries/{name}/books"), HttpMethod.POST);
    final Link getBookByIsbn = new Link("get-book", toUri("http://host.domain.com/service/v1/libraries/{name}/books/{isbn}"));
    final Link getBookByTitle = new Link("get-book", toUri("http://host.domain.com/service/v1/libraries/{name}/books/{title}"));
    final Link removeBook = new Link("remove-book", toUri("http://host.domain.com/service/v1/libraries/{name}/books/{isbn}"), HttpMethod.DELETE);

    linkIndex = new LinkIndex();
    linkIndex.addAll(listLibraries, getLibrary, listBooks, listBooksByAuthor, listBooksByAuthorAndCategory,
      listBooksByAuthorAndYear, listBooksByAuthorCategoryAndYear, addBook, getBookByIsbn, getBookByTitle, removeBook);

    assertFalse(linkIndex.isEmpty());
    assertEquals(11, linkIndex.size());

    operationInvoker = new RestHttpOperationInvoker(linkIndex);

    assertSame(linkIndex, operationInvoker.getLinkIndex());
  }

  @After
  public void tearDown() {
    operationInvoker.stop();
    operationInvoker = null;
  }

  protected CommandRequest createCommandRequest(final String command, final Map<String, String> options) {
    return new TestCommandRequest(command, options);
  }

  protected CommandRequest createCommandRequest(final String command, final Map<String, String> options, final byte[][] fileData) {
    return new TestCommandRequest(command, options, fileData);
  }

  protected LinkIndex getLinkIndex() {
    assertTrue("The LinkIndex was not property initialized!", linkIndex != null);
    return linkIndex;
  }

  protected RestHttpOperationInvoker getOperationInvoker() {
    assertTrue("The RestHttpOperationInvoker was not properly initialized!", operationInvoker != null);
    return operationInvoker;
  }

  @Test
  public void testCreateHttpRequest() {
    final Map<String, String> commandOptions = new HashMap<String, String>();

    commandOptions.put("author", "Adams");
    commandOptions.put("blankOption", "  ");
    commandOptions.put("category", "sci-fi");
    commandOptions.put("emptyOption", StringUtils.EMPTY_STRING);
    commandOptions.put("isbn", "0-123456789");
    commandOptions.put("nullOption", null);
    commandOptions.put("title", "Hitch Hiker's Guide to the Galaxy");
    commandOptions.put("year", "1983");

    final CommandRequest command = createCommandRequest("add-book", commandOptions);

    final ClientHttpRequest request = getOperationInvoker().createHttpRequest(command);

    assertNotNull(request);
    assertNotNull(request.getLink());
    assertEquals("POST http://host.domain.com/service/v1/libraries/{name}/books", request.getLink().toHttpRequestLine());
    assertEquals("Adams", request.getRequestParameterValue("author"));
    assertEquals("sci-fi", request.getRequestParameterValue("category"));
    assertEquals("0-123456789", request.getRequestParameterValue("isbn"));
    assertEquals("Hitch Hiker's Guide to the Galaxy", request.getRequestParameterValue("title"));
    assertEquals("1983", request.getRequestParameterValue("year"));
    assertFalse(request.getParameters().containsKey("blankOption"));
    assertFalse(request.getParameters().containsKey("emptyOption"));
    assertFalse(request.getParameters().containsKey("nullOption"));
  }

  @Test
  public void testCreatHttpRequestWithFileData() {
    final Map<String, String> commandOptions = Collections.singletonMap("isbn", "0-123456789");

    final byte[][] fileData = { "/path/to/book/content.txt".getBytes(),
      "Once upon a time in a galaxy far, far away...".getBytes() };

    final CommandRequest command = createCommandRequest("add-book", commandOptions, fileData);

    final ClientHttpRequest request = getOperationInvoker().createHttpRequest(command);

    assertNotNull(request);
    assertNotNull(request.getLink());
    assertEquals("POST http://host.domain.com/service/v1/libraries/{name}/books", request.getLink().toHttpRequestLine());
    assertEquals("0-123456789", request.getRequestParameterValue("isbn"));
    assertTrue(request.getParameters().containsKey(RestHttpOperationInvoker.RESOURCES_REQUEST_PARAMETER));
    assertTrue(request.getRequestParameterValue(RestHttpOperationInvoker.RESOURCES_REQUEST_PARAMETER) instanceof Resource);

    final List<Object> resources = request.getRequestParameterValues(RestHttpOperationInvoker.RESOURCES_REQUEST_PARAMETER);

    assertNotNull(resources);
    assertFalse(resources.isEmpty());
    assertEquals(1, resources.size());
  }

  @Test
  public void testFindAndResolveLink() throws Exception {
    final Map<String, String> commandOptions = new HashMap<String, String>();

    commandOptions.put("name", "BarnesN'Noble");

    Link link = getOperationInvoker().findLink(createCommandRequest("list-libraries", commandOptions));

    assertNotNull(link);
    assertEquals("http://host.domain.com/service/v1/libraries", toString(link.getHref()));

    link = getOperationInvoker().findLink(createCommandRequest("get-library", commandOptions));

    assertNotNull(link);
    assertEquals("http://host.domain.com/service/v1/libraries/{name}", toString(link.getHref()));

    commandOptions.put("author", "J.K.Rowlings");

    link = getOperationInvoker().findLink(createCommandRequest("list-books", commandOptions));

    assertNotNull(link);
    assertEquals("http://host.domain.com/service/v1/libraries/{name}/books/{author}", toString(link.getHref()));

    commandOptions.put("category", "sci-fi");
    commandOptions.put("year", "1998");
    commandOptions.put("bogus", "data");

    link = getOperationInvoker().findLink(createCommandRequest("list-books", commandOptions));

    assertNotNull(link);
    assertEquals("http://host.domain.com/service/v1/libraries/{name}/books/{author}/{category}/{year}",
      toString(link.getHref()));

    commandOptions.remove("category");

    link = getOperationInvoker().findLink(createCommandRequest("list-books", commandOptions));

    assertNotNull(link);
    assertEquals("http://host.domain.com/service/v1/libraries/{name}/books/{author}/{year}",
      toString(link.getHref()));

    commandOptions.put("category", "fantasy");
    commandOptions.put("isbn", "0-123456789");
    commandOptions.put("title", "Harry Potter");

    link = getOperationInvoker().findLink(createCommandRequest("add-book", commandOptions));

    assertNotNull(link);
    assertEquals("http://host.domain.com/service/v1/libraries/{name}/books", toString(link.getHref()));

    commandOptions.remove("isbn");

    link = getOperationInvoker().findLink(createCommandRequest("get-book", commandOptions));

    assertNotNull(link);
    assertEquals("http://host.domain.com/service/v1/libraries/{name}/books/{title}", toString(link.getHref()));

    link = getOperationInvoker().findLink(createCommandRequest("remove-book", commandOptions));

    assertNotNull(link);
    assertEquals("http://host.domain.com/service/v1/libraries/{name}/books/{isbn}", toString(link.getHref()));
  }

  @Test
  public void testProcessCommand() {
    final String expectedResult = "{\"libraries\":[{\"library-of\":\"Congress\"}]"; // JSON

    final RestHttpOperationInvoker operationInvoker = new RestHttpOperationInvoker(getLinkIndex()) {
      @Override
      public boolean isConnected() {
        return true;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected <T> ResponseEntity<T> send(final ClientHttpRequest request, final Class<T> responseType, final Map<String, ?> uriVariables) {
        return new ResponseEntity(expectedResult, HttpStatus.OK);
      }
    };

    final String actualResult = operationInvoker.processCommand(createCommandRequest("list-libraries",
      Collections.<String, String>emptyMap()));

    assertEquals(expectedResult, actualResult);
  }

  @Test
  public void testProcessCommandDelegatesToSimpleProcessCommand() {
    final String expectedResult = "<resources>test</resources>";

    final RestHttpOperationInvoker operationInvoker = new RestHttpOperationInvoker(getLinkIndex()) {
      @Override
      public boolean isConnected() {
        return true;
      }

      @Override
      protected HttpOperationInvoker getHttpOperationInvoker() {
        return new AbstractHttpOperationInvoker(AbstractHttpOperationInvoker.REST_API_URL) {
          @Override public Object processCommand(final CommandRequest command) {
            return expectedResult;
          }
        };
      }

      @Override
      protected void printWarning(final String message, final Object... args) {
      }
    };

    final String actualResult = operationInvoker.processCommand(createCommandRequest("get resource",
      Collections.<String, String>emptyMap()));

    assertEquals(expectedResult, actualResult);
  }

  @Test
  public void testProcessCommandHandlesResourceAccessException() {
    final RestHttpOperationInvoker operationInvoker = new RestHttpOperationInvoker(getLinkIndex()) {
      private boolean connected = true;

      @Override
      public boolean isConnected() {
        return connected;
      }

      @Override
      protected void printWarning(final String message, final Object... args) {
      }

      @Override
      protected <T> ResponseEntity<T> send(final ClientHttpRequest request, final Class<T> responseType, final Map<String, ?> uriVariables) {
        throw new ResourceAccessException("test");
      }

      @Override
      public void stop() {
        this.connected = false;
      }
    };

    assertTrue(operationInvoker.isConnected());

    final String expectedResult = String.format(
      "The connection to the GemFire Manager's HTTP service @ %1$s failed with: %2$s. "
        + "Please try reconnecting or see the GemFire Manager's log file for further details.",
      operationInvoker.getBaseUrl(), "test");

    final String actualResult = operationInvoker.processCommand(createCommandRequest("list-libraries",
      Collections.<String, String>emptyMap()));

    assertFalse(operationInvoker.isConnected());
    assertEquals(expectedResult, actualResult);
  }

  @Test(expected = RestApiCallForCommandNotFoundException.class)
  public void testProcessCommandThrowsRestApiCallForCommandNotFoundException() {
    final RestHttpOperationInvoker operationInvoker = new RestHttpOperationInvoker(getLinkIndex()) {
      @Override
      public boolean isConnected() {
        return true;
      }

      @Override
      protected HttpOperationInvoker getHttpOperationInvoker() {
        return null;
      }
    };

    try {
      operationInvoker.processCommand(createCommandRequest("get resource", Collections.<String, String>emptyMap()));
    }
    catch (RestApiCallForCommandNotFoundException e) {
      assertEquals("No REST API call for command (get resource) was found!", e.getMessage());
      throw e;
    }
  }

  @Test(expected = AssertionError.class)
  public void testProcessCommandWhenNotConnected() {
    try {
      getOperationInvoker().processCommand(createCommandRequest("get-book", Collections.<String, String>emptyMap()));
    }
    catch (AssertionError e) {
      assertEquals("Gfsh must be connected to the GemFire Manager in order to process commands remotely!",
        e.getMessage());
      throw e;
    }
  }

  protected static final class TestCommandRequest extends CommandRequest {

    private final byte[][] fileData;

    private final Map<String, String> commandParameters = new TreeMap<String, String>();

    private final String command;

    protected TestCommandRequest(final String command, final Map<String, String> commandParameters) {
      this(command, commandParameters, null);
    }

    protected TestCommandRequest(final String command, final Map<String, String> commandParameters, final byte[][] fileData) {
      super(Collections.<String, String>emptyMap());

      assert command != null : "The command cannot be null!";

      this.command = command;

      if (commandParameters != null) {
        this.commandParameters.putAll(commandParameters);
      }

      this.fileData = fileData;
    }

    @Override
    public byte[][] getFileData() {
      return fileData;
    }

    @Override
    public String getInput() {
      return command;
    }

    @Override
    public String getName() {
      return command;
    }

    @Override
    public Map<String, String> getParameters() {
      return Collections.unmodifiableMap(commandParameters);
    }
  }

}
