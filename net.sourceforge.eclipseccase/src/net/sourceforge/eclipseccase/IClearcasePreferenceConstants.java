/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas taken from Eclipse code
 *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
 *******************************************************************************/
package net.sourceforge.eclipseccase;

/**
 * Shared preference constants for ClearCase plugin preferences.
 */
public interface IClearcasePreferenceConstants
{
    /** ClearCase preference */
    String ADD_AUTO = ClearcasePlugin.PLUGIN_ID
            + ".add.auto";

    /** ClearCase preference */
    String ADD_WITH_CHECKIN = ClearcasePlugin.PLUGIN_ID
            + ".add.checkin";

    /** ClearCase preference */
    String CHECKOUT_AUTO = ClearcasePlugin.PLUGIN_ID
            + ".checkout.auto";

    /** ClearCase preference */
    String CHECKOUT_LATEST = ClearcasePlugin.PLUGIN_ID
            + ".checkout.latest";

    /** ClearCase preference */
    String CHECKOUT_RESERVED = ClearcasePlugin.PLUGIN_ID
            + ".checkout.reserved";

    /** ClearCase preference */
    String COMMENT_ADD = ClearcasePlugin.PLUGIN_ID
            + ".comment.add";

    /** ClearCase preference */
    String COMMENT_ADD_NEVER_ON_AUTO = ClearcasePlugin.PLUGIN_ID
            + ".comment.add";

    /** ClearCase preference */
    String COMMENT_CHECKIN = ClearcasePlugin.PLUGIN_ID
            + ".comment.checkin";

    /** ClearCase preference */
    String COMMENT_CHECKOUT = ClearcasePlugin.PLUGIN_ID
            + ".comment.checkout";

    /** ClearCase preference */
    String COMMENT_CHECKOUT_NEVER_ON_AUTO = ClearcasePlugin.PLUGIN_ID
            + ".comment.checkout.neverOnAuto";

    /** ClearCase preference */
    String COMMENT_ESCAPE = ClearcasePlugin.PLUGIN_ID
            + ".comment.escape";

    /** ClearCase preference */
    String IGNORE_NEW = ClearcasePlugin.PLUGIN_ID
            + ".ignore.new";

    /** ClearCase preference */
    String PRESERVE_TIMES = ClearcasePlugin.PLUGIN_ID
            + ".preserveTimes";

    /** ClearCase preference */
    String RECURSIVE = ClearcasePlugin.PLUGIN_ID
            + ".recursive";


    /** common preference */
    String SAVE_DIRTY_EDITORS = ClearcasePlugin.PLUGIN_ID
            + ".saveDirtyEditors";

    /** ClearCase preference */
    String USE_CLEARTOOL = ClearcasePlugin.PLUGIN_ID
            + ".useCleartool";

    /** preference value */
    String VALUE_ASK = "ask";

    /** preference value */
    String VALUE_FORCE = "force";

    /** preference value */
    String VALUE_IF_POSSIBLE = "ifPossible";

    /** preference value */
    String VALUE_NEVER = "never";
}