/*
 * Copyright (c) 2003 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */


  var MapMode = new Array();
  MapMode["merge"] = 0;
  MapMode["overwrite"] = 1;
  MapMode["discard"] = 2;

  MapMode["add_as_new"] = 0;
  MapMode["sync_on_concept"] = 1;
  MapMode["sync_on_language"] = 2;

  var Mode = new Array();
  Mode[0] = "add_as_new";
  Mode[1] = "sync_on_concept";
  Mode[2] = "sync_on_language";

  var Action = new Array();
  Action[0] = "merge";
  Action[1] = "overwrite";
  Action[2] = "discard";
