/**
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.odata4j.producer;

import java.util.Collection;

import org.odata4j.core.OEntityId;
import org.odata4j.edm.EdmMultiplicity;

/**
 * An <code>EntityIdResponse</code> is a response to a client request expecting one or more entity-ids.
 *
 * <p>The {@link Responses} static factory class can be used to create <code>EntityIdResponse</code> instances.</p>
 */
public interface EntityIdResponse {

  /** Gets the multiplicity of the response */
  EdmMultiplicity getMultiplicity();

  /** Gets the response payload as entity ids */
  Collection<OEntityId> getEntities();

}
