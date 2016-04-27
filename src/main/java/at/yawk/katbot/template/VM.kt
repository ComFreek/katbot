/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.katbot.template

/**
 * @author yawkat
 */
interface VM {
    fun invoke(parameters: List<Expression>): List<String>

    /**
     * Return a copy of this VM with the given additional high-priority functions.
     */
    fun plusFunctions(functions: List<Function>): VM
}