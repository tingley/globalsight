Here go classes that define the UI Data Model.

The UI Data Model holds display strings and listener associations 
that allow the UI to react to user events. The data model itself 
is passive. It does not initiate anything, nor does it know how to 
make itself look "right". This is the task of two other components: 

- the model creators (in galign.helpers.*) that instantiate
  model objects from domain data (GAP, GAM, etc)
- the controller classes which implement the control flow
  by instantiating models (using helpers) and setting up
  action listeners.

Model classes must be kept separate, and stupid.

