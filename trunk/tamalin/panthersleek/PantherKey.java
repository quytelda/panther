/*
 * Copyright 2010 Quytelda Gaiwin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tamalin.panthersleek;

import java.io.Serializable;
import java.security.*;

/**
 *
 * @author Quytelda K. Gaiwin
 */
public class PantherKey implements Serializable
{

  public PantherKey(Key k, int idNumber, String dispName)
  {
    key = k;
    id = idNumber;
    displayName = dispName;
  }

  public Key getKey()
  {
    return key;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public int getId()
  {
    return id;
  }

  @Override
  public String toString()
  {
    return this.getClass().getName() + "[displayName=" + displayName + "&key=" + key.toString() + "&id=" + id + "]";
  }

  @Override
  public int hashCode()
  {
    return this.toString().hashCode();
  }

  @Override
  public boolean equals(Object other)
  {
    if (other instanceof PantherKey)
    {
      PantherKey otherKey = (PantherKey) other;
      return (displayName.equals(otherKey.getDisplayName()) && key.equals(otherKey.getKey()) && id == otherKey.getId());
    }
    return false;
  }

  Key key;
  String displayName = "";
  int id = 0;
}
