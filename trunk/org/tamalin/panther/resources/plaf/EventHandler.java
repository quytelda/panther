/*
 * Copyright 2011 Quytelda K. Gaiwin
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

package org.tamalin.panther.resources.plaf;
/**
 * The EventHandler class handles events that differ from platform to platform
 * in behavior, for example, closing the frame, which exits the application on
 * Linux and Windows, but merely hides the application on Mac OS X.
 * Date: Dec 8, 2010
 * Time: 12:33:55 PM
 */

/**
 * @author Quytelda K. Gaiwin
 * @since 4.0
 */

public interface EventHandler
{
    public boolean eventTriggered();
}
