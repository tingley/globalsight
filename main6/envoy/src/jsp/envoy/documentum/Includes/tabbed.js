/******************************************************************************
* Common code for tabbed pages.                                               *
******************************************************************************/

// Call the synchTab() function in the parent window.

if (window.parent && window.parent.synchTab)
  window.parent.synchTab(window.name);
