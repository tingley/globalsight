Example: Extending forms in NetBeans form editor

BasePanelForm.java, BasePanelForm.form
  - This is the base form to be extended by other forms.
  - It contains three buttons and a central subpanel.
  - The central panel serves as the "container delegate" - the area where
    new components should be added in the extending forms.
  - Note: container delegate is empty, the buttons are out of it.

BasePanelFormBeanInfo.java
  - Defines the "container delegate" of the BasePanelForm.

ExtendingPanelForm.java, ExtendingPanelForm.form
  - An extending form based on BasePanelForm.
  - When opened, BasePanelForm is visible in the designer (with the
    three buttons).
  - You can add new components to it and change its layout - the central
    subpanel mentioned above is affected by this.
  - The three buttons from BasePanelForm are not accessible, cannot be
    changed from here.


Any change in the BasePanelForm is reflected in the extending forms.
(BasePanelForm must be recompiled and the extending forms reloaded.)
