
var objLocalTree = null;

var NODE_COUNT = 0;

function jsTree()
{
    //Public Properties
    this.root = null;           //the root node of the tree

     //Public Collections
    this.nodes = new Array;     //array for all nodes in the tree

    //Constructor
    //assign to local copy of the tree
    objLocalTree = this;
}

jsTree.prototype.createRoot = function(strText, strId, check)
{
    //create a new node
    this.root = new jsTreeNode(strText, strId, null, null, check);

    //assign an ID for internal tracking
    this.root.id = "root";

    //add it into the array of all nodes
    this.nodes["root"] = this.root;

    //make sure that the root is expanded
    this.root.expanded = true;

    //return the created node
    return this.root;
}

jsTree.prototype.openAllNodes = function()
{
    this.root.openNode();
    for (var i = 0; i < this.root.childNodes.length; i++)
    {
        this.root.childNodes[i].openNode();
    }
}

jsTree.prototype.closeAllNodes = function()
{
    this.root.closeNode();
    for (var i = 0; i < this.root.childNodes.length; i++)
    {
        this.root.childNodes[i].closeNode();
    }
}

function jsTreeNode(strText, strId, type, parent, set, count)
{
    //Public Properties
    this.text = strText;            //the text to display
    this.value = strId;             //the id of the permission or category
    this.type = type;               //the type: permission or category
    this.parent = parent;           //the parent of this node
    this.set = set;                 //is this permission set
    this.count = count;             //needed to make unique node name
    if (parent && set == "true") parent.set = set;

    //Public States
    this.expanded = false;          //is this node expanded?

    //Public Collections
    this.childNodes = new Array;    //the collection of child nodes

}

jsTreeNode.prototype.addChild = function (strText, strId, type, set)
{
    //create a new node
    var objNode = new jsTreeNode(strText, strId, type, this, set, NODE_COUNT++);

    //assign an ID for internal tracking
    objNode.id = this.id + "_" + this.childNodes.length;

    //add into the array of child nodes
    this.childNodes[this.childNodes.length] = objNode;

    //add it into the array of all nodes
    objLocalTree.nodes[objNode.id] = objNode;

    //return the created node
    return objNode;
}


// Writes this node to the DOM and recurses on it's children
jsTreeNode.prototype.writeNode = function (disabled)
{
    if (this.childNodes.length == 0)
    {
        document.write("<div>" + this.text + "&nbsp;<input type=checkbox id='" + this.value + "' name='" + this.value +  "' value=" + this.value);
        if (this.set == "true") document.write(" checked ");
        if (disabled) document.write(" disabled ");
        document.writeln(" onclick=\"updateCB(this)\"></div>");
    }
    else
    {
        if (this.id == "root")
        {
            document.write("<div id=\"root\" class=\"root\">");
            document.writeln("<a  href=\"\" onclick=\"toggleNode(this.parentNode, this); return false;\">");
            document.writeln("<img src=/globalsight/images/plus.gif border=0></a>");
        }
        else
        {
            document.writeln("<div id=\"" + this.text + this.count +"\" >");
            document.writeln("<a  href=\"\" onclick=\"toggleNode(this.parentNode, this); return false;\">");
            document.writeln("<img src=/globalsight/images/plus.gif border=0></a>");
        }

        if (this.type == "cat") document.writeln("<b>" + this.text + "</b>");
        else document.writeln(this.text);
        document.write("&nbsp;<input type=checkbox id='" + this.value + "' name='" + this.value +  "' value=" + this.value);
        if (this.set == "true") document.write(" checked ");
        if (disabled) document.write(" disabled ");
        document.writeln(" onclick=\"updateCB(this)\">");
    }

    for (var i = 0; i < this.childNodes.length; i++)
    {
        this.childNodes[i].writeNode(disabled);
    }
    if (this.childNodes.length > 0)
    {
        document.writeln("</div>  <!-- end " + this.text + "-->");
    }
}

jsTreeNode.prototype.openNode = function ()
{
    obj = document.getElementById(this.text + this.count)
    if (obj)
    {
        openNode(obj);
    }
    else
    {
        obj = document.getElementById("root");
        if (obj)
        {
            openNode(obj);
        }
    }
    for (var i = 0; i < this.childNodes.length; i++)
    {
        this.childNodes[i].openNode();
    }
}

jsTreeNode.prototype.closeNode = function ()
{
    obj = document.getElementById(this.text + this.count)
    if (obj)
    {
        closeNode(obj);
    }
    else
    {
        obj = document.getElementById("root");
        if (obj)
        {
            closeNode(obj);
        }
    }
    for (var i = 0; i < this.childNodes.length; i++)
    {
        this.childNodes[i].closeNode();
    }
}

jsTreeNode.prototype.findNode = function (idStr)
{
    if (this.value == idStr)
    {
        return this;
    }

    for (var i = 0; i < this.childNodes.length; i++)
    {
        found = this.childNodes[i].findNode(idStr);
        if (found) return found;
    }
}

jsTreeNode.prototype.updateChildren = function (check)
{
    cb = document.getElementById(this.value);

    if (cb.disabled == false)
    	cb.checked = check;

    for (var i = 0; i < this.childNodes.length; i++)
    {
        this.childNodes[i].updateChildren(check);
    }
}

jsTreeNode.prototype.checkParents = function ()
{
    if (this.parent != null)
    {
        cb = document.getElementById(this.parent.value);
        cb.checked = true;
        set = true;
        this.parent.checkParents();
    }
}

jsTreeNode.prototype.uncheckParent = function ()
{
    if (this.parent != null && this.parent.type == "cat" )
    {
        var childChecked = false;
        for (var i = 0; i < this.parent.childNodes.length; i++)
        {
            cb = document.getElementById(this.parent.childNodes[i].value);
            if (cb.checked == true)
            {
                childChecked = true;
                break;
            }
        }
        if (childChecked == false)
        {
            cb = document.getElementById(this.parent.value);
            cb.checked = false;
            set = false;
        }
    }
}

function toggleNode(node, imgNode)
{
    // first update the image
    if (imgNode.innerHTML.indexOf("minus.gif") == -1)
    {
        imgNode.innerHTML = "<img src=/globalsight/images/minus.gif border=0>";
    }
    else
    {
        imgNode.innerHTML = "<img src=/globalsight/images/plus.gif border=0>";
    }

    var nodeArray = node.childNodes;
    for (var i = 0; i < nodeArray.length; i++)
    {
        node = nodeArray[i];
        if (node.tagName && node.tagName.toLowerCase() == 'div')
        {
            node.style.display = (node.style.display == 'block') ? 'none' : 'block';
        }
    }
}


function openNode(node)
{
    var nodeArray = node.childNodes;
	var isFirst = true;
    for (var i = 0; i < nodeArray.length; i++)
    {
        node = nodeArray[i];
		if (node.nodeType == 1 && isFirst) {
        // The first child is the image
            node.innerHTML = "<img src=/globalsight/images/minus.gif border=0>";
			isFirst = false;
        } 

        if (node.tagName && node.tagName.toLowerCase() == 'div')
        {
		  
           node.style.display = 'block';
        }
    }
}

function closeNode(node)
{
    var nodeArray = node.childNodes;
	var isFirst = true;
    for (var i = 0; i < nodeArray.length; i++)
    {
        node = nodeArray[i];
        // The first child is the image
        if (node.nodeType == 1 && isFirst)
        {
            node.innerHTML = "<img src=/globalsight/images/plus.gif border=0>";
			isFirst = false;
        }
        if (node.tagName && node.tagName.toLowerCase() == 'div')
        {
            node.style.display = 'none';
        }
    }
}

function showNode(node)
{
    if (!node) return;
    if (!node.parentNode) return;
    node = node.parentNode;
    while (node.tagName.toLowerCase() == 'div')
    {
        openNode(node);
        node = node.parentNode;
    }
}

//
// Check and uncheck boxes appropriately.
// If checking checkbox, make sure it's parent is checked.  Also if it
// is a category, automatically select all it's children.
// If unchecking a checkbox, go to the parent and uncheck it if all it's
// other children are unchecked.
var theCB = null;
function updateCB(obj)
{
    idBody.style.cursor = "wait";
    theCB = obj;
    setTimeout("doIt()", 1);
}

function doIt()
{
    obj = theCB;
    node = objLocalTree.root.findNode(obj.value);
    // Either check or uncheck all children
    node.updateChildren(obj.checked);

    // Make sure all parents are checked
    cb = document.getElementById(node.value);
    if (obj.checked) node.checkParents();

    // Unchecking.  If the parent is a catergory,
    // check it's children and if none remain checked, uncheck it.
    if (obj.checked == false) node.uncheckParent();

    idBody.style.cursor = "default";
}
