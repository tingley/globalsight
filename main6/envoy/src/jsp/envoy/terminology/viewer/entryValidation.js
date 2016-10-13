//
// Input Model Verifier
//

// import getFieldNameByType() from ../management/objects_js.jsp

function InputModelValidationResult(html, message)
{
    // The node that can be click()'ed to be selected.
    this.html = html;
    // Error message (if node is present).
    this.message = message;
}

InputModelValidationResult.prototype.getHtml = function ()
{
    return this.html;
}

InputModelValidationResult.prototype.getMessage = function ()
{
    return this.message;
}

function InputModelValidator(xml, html, inputmodel, termbaseFields)
{
    this.xml = xml;
    this.html = html;
    this.model = inputmodel;
    this.termbaseFields = termbaseFields;
    this.result = null;

    this.debugEnabled = false;
}

InputModelValidator.prototype.debug = function (arg)
{
    if (this.debugEnabled) alert(arg);
}

InputModelValidator.prototype.validate = function ()
{
    // this.debug("Validating with model " + this.model.xml);

    if (!this.html || !this.html.children || this.html.children.length == 0)
    {
        return new InputModelValidationResult(this.html,
            "Entry is empty.");
    }

    try
    {
        // First, walk the model and see if all mandatory terms and
        // fields are present. Also check synonym existence and
        // multiplicity.

        this.validateConceptGrp(this.model.selectSingleNode('conceptGrp'),
            this.html);

        // Second, check all fields considered implicitly allowed:
        // find field (types) that are not defined in the termbase
        // (and report them as warning???), and check attribute field
        // values.

        this.checkAttrConceptGrp(this.html);
    }
    catch (ex)
    {
        if (!ex.html)
        {
            // JS error, may need to switch off validation in
            // production mode.
            ex = new InputModelValidationResult(this.html,
                "Javascript error: " + ex.message);
        }

        this.result = ex;
    }

    return this.result;
}

InputModelValidator.prototype.validateConceptGrp = function (mnode, hnode)
{
    var children = mnode.childNodes;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        if (child.nodeName == 'descripGrp')
        {
            this.validateDescripGrp(child, hnode);
        }
        else if (child.nodeName == 'sourceGrp')
        {
            this.validateSourceGrp(child, hnode);
        }
        else if (child.nodeName == 'noteGrp')
        {
            this.validateNoteGrp(child, hnode);
        }
        else if (child.nodeName == 'languageGrp')
        {
            this.validateLanguageGrp(child, hnode);
        }
    }
}

InputModelValidator.prototype.validateNoteGrp = function (mnode, hnode)
{
    var constraint = mnode.selectSingleNode('note').text;
    var required = constraint.indexOf('required') >= 0;
    var multiple = constraint.indexOf('multiple') >= 0;

    var fields = this.findNoteGrps(hnode);
    var field  = fields[0];

    if (required)
    {
        if (!field || this.isEmptyField(field))
        {
            throw new InputModelValidationResult(hnode,
                "Required note is missing or empty.");
        }
    }
    else // optional
    {
        if (!field) return;
    }

    if (!multiple && fields.length > 1)
    {
        throw new InputModelValidationResult(field[1],
            "Multiple notes are not allowed.");
    }

    // no sub nodes.
}

InputModelValidator.prototype.validateSourceGrp = function (mnode, hnode)
{
    var constraint = mnode.selectSingleNode('source').text;
    var required = constraint.indexOf('required') >= 0;
    var multiple = constraint.indexOf('multiple') >= 0;

    var fields = this.findSourceGrps(hnode);
    var field  = fields[0];

    if (required)
    {
        if (!field || this.isEmptyField(field))
        {
            throw new InputModelValidationResult(hnode,
                "Required source is missing or empty.");
        }
    }
    else // optional
    {
        if (!field) return;
    }

    if (!multiple && fields.length > 1)
    {
        throw new InputModelValidationResult(fields[1],
            "Multiple sources are not allowed.");
    }

    var childConstraint = mnode.selectSingleNode('noteGrp');
    if (childConstraint)
    {
        for (var i = 0; i < fields.length; i++)
        {
            field = fields[i];
            this.validateNoteGrp(childConstraint, field);
        }
    }
}

InputModelValidator.prototype.validateDescripGrp = function (mnode, hnode)
{
    var constraint = mnode.selectSingleNode('descrip').text;
    var required = constraint.indexOf('required') >= 0;
    var multiple = constraint.indexOf('multiple') >= 0;

    var type = getAtrributeText(mnode.selectSingleNode('descrip/@type'));
    var fields = this.findDescripGrps(hnode, type);
    var field  = fields[0];

    if (required)
    {
        if (!field || this.isEmptyField(field))
        {
            var displayType = getFieldNameByType(type, this.termbaseFields);
            throw new InputModelValidationResult(hnode,
                "Required field \"" + displayType + "\" is missing or empty.");
        }
    }
    else // optional
    {
        if (!field) return;
    }

    if (!multiple && fields.length > 1)
    {
        var displayType = getFieldNameByType(type, this.termbaseFields);
        throw new InputModelValidationResult(fields[1],
            "Multiple fields of type \"" + displayType + "\" are not allowed.");
    }

    var childConstraint = mnode.selectSingleNode('sourceGrp');
    if (childConstraint)
    {
        for (var i = 0; i < fields.length; i++)
        {
            field = fields[i];
            this.validateSourceGrp(childConstraint, field);
        }
    }

    var childConstraint = mnode.selectSingleNode('noteGrp');
    if (childConstraint)
    {
        for (var i = 0; i < fields.length; i++)
        {
            field = fields[i];
            this.validateNoteGrp(childConstraint, field);
        }
    }
}

InputModelValidator.prototype.validateLanguageGrp = function (mnode, hnode)
{
    // only one languageGrp per language
    var constraint = mnode.selectSingleNode('language').text;

    var name = getAtrributeText(mnode.selectSingleNode('language/@name'));
    var field = this.findLanguageGrp(hnode, name);

    if (!field)
    {
        throw new InputModelValidationResult(hnode,
            "Required language \"" + name + "\" is missing.");
    }

    var children = mnode.childNodes;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        if (child.nodeName == 'descripGrp')
        {
            this.validateDescripGrp(child, field);
        }
        else if (child.nodeName == 'sourceGrp')
        {
            this.validateSourceGrp(child, field);
        }
        else if (child.nodeName == 'noteGrp')
        {
            this.validateNoteGrp(child, field);
        }
    }

    var childConstraints = mnode.selectNodes('termGrp');
    if (childConstraints)
    {
        this.validateTermGrps(childConstraints, field, name);
    }
}

InputModelValidator.prototype.validateTermGrps = function (mnodes, hnode, name)
{
    // multiple termGrps per languageGrp: main term and synonyms
    var mainNode = mnodes[0];
    var synonymNode = (mnodes.length > 1 ? mnodes[1] : null);

    var constraint = mainNode.selectSingleNode('term').text;
    var required = constraint.indexOf('required') >= 0;

    var fields = this.findTermGrps(hnode);
    var field  = fields[0];

    // First term is main term.

    // Superfluous check, already caught in languageGrp.
    if (!field)
    {
        throw new InputModelValidationResult(hnode,
            "Required term is missing in language \"" + name + "\".");
    }

    this.validateTermGrp(mainNode, field);

    // Now for the synonyms.

    if (!synonymNode && fields.length > 1)
    {
        throw new InputModelValidationResult(fields[1],
            "Synonyms are not allowed in language \"" + name + "\".");
    }

    if (synonymNode)
    {
        var constraint = synonymNode.selectSingleNode('term').text;

        var required = constraint.indexOf('required') >= 0;
        var multiple = constraint.indexOf('multiple') >= 0;

        // remove main term from the list of all terms
        fields.splice(0, 1);
        field = fields[0];

        if (required)
        {
            if (!field || this.isEmptyField(field.firstChild))
            {
                throw new InputModelValidationResult(hnode,
                    "Required synonym is missing in language \"" + name + "\".");
            }
        }
        else
        {
            if (!field) return;
        }

        if (!multiple && fields.length > 1)
        {
            throw new InputModelValidationResult(field[1],
                "Multiple synonyms are not allowed in language \"" + name + "\".");
        }

        for (var i = 0; i < fields.length; i++)
        {
            field = fields[i];

            this.validateTermGrp(synonymNode, field);
        }
    }
}

InputModelValidator.prototype.validateTermGrp = function (mnode, hnode)
{
    // Main Term and synonyms already checked by caller
    var children = mnode.childNodes;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        if (child.nodeName == 'descripGrp')
        {
            this.validateDescripGrp(child, hnode);
        }
        else if (child.nodeName == 'sourceGrp')
        {
            this.validateSourceGrp(child, hnode);
        }
        else if (child.nodeName == 'noteGrp')
        {
            this.validateNoteGrp(child, hnode);
        }
    }
}

//
// Attribute Checking
//

InputModelValidator.prototype.checkAttrConceptGrp = function (hnode)
{
    this.checkAttrDescripGrps(hnode);
    this.checkAttrLanguageGrps(hnode);
}

InputModelValidator.prototype.checkAttrDescripGrp = function (hnode)
{
    var descType = hnode.firstChild.getAttribute("type");
    var descValue = this.trim(hnode.children[1].innerHTML);

    if (getFieldFormatByType(descType) == "attr")
    {
        // If the field is defined by the user in the termbase...
        var field = getFieldByType(descType, this.termbaseFields);
        if (field)
        {
            // This descrip is defined in the termbase, check the
            // attribute values.

            var values = field.getValues();

            if (!this.isValidAttributeValue(values, descValue))
            {
                var displayType = getFieldNameByType(descType,
                    this.termbaseFields);
                throw new InputModelValidationResult(hnode,
                    "The attribute \"" + displayType +
                    "\" has an invalid value \"" + descValue + "\".");
            }
        }
    }
}

InputModelValidator.prototype.checkAttrDescripGrps = function (hnode)
{
    var fields = this.findDescripGrps(hnode);

    for (var i = 0; i < fields.length; i++)
    {
        var field = fields[i];

        this.checkAttrDescripGrp(field);
    }
}

InputModelValidator.prototype.checkAttrLanguageGrps = function (hnode)
{
    var fields = this.findLanguageGrps(hnode);

    for (var i = 0; i < fields.length; i++)
    {
        var field = fields[i];

        this.checkAttrDescripGrps(field);
        this.checkAttrTermGrps(field);
    }
}

InputModelValidator.prototype.checkAttrTermGrps = function (hnode)
{
    var fields = this.findTermGrps(hnode);

    for (var i = 0; i < fields.length; i++)
    {
        var field = fields[i];

        this.checkAttrDescripGrps(field);
    }
}

//
// HTML accessors
//

InputModelValidator.prototype.findNoteGrps = function (hnode)
{
    var result = new Array();

    var children = hnode.children;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        var fieldType = getFieldType(child);
        if (fieldType == 'fieldGrp' && child.firstChild.getAttribute("type") == 'note')
        {
            result.push(child);
        }
    }

    return result;
}

InputModelValidator.prototype.findSourceGrps = function (hnode)
{
    var result = new Array();

    var children = hnode.children;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        var fieldType = getFieldType(child);
        if (fieldType == 'fieldGrp' && child.firstChild.getAttribute("type") == 'source')
        {
            result.push(child);
        }
    }

    return result;
}

InputModelValidator.prototype.findDescripGrps = function (hnode, type)
{
    var result = new Array();

    if (typeof(type) == "undefined")
    {
        // Find all descripGrps.
        var children = hnode.children;
        for (var i = 0; i < children.length; i++)
        {
            var child = children[i];

            var fieldType = getFieldType(child);
            if (fieldType == 'fieldGrp')
            {
                var t = child.firstChild.getAttribute("type");

                if (t != 'note' && t != 'source')
                {
                    result.push(child);
                }
            }
        }
    }
    else
    {
        // Find all descripGrps of a certain type.
        var children = hnode.children;
        for (var i = 0; i < children.length; i++)
        {
            var child = children[i];

            var fieldType = getFieldType(child);
            if (fieldType == 'fieldGrp' && child.firstChild.getAttribute("type") == type)
            {
                result.push(child);
            }
        }
    }

    return result;
}

InputModelValidator.prototype.findLanguageGrp = function (hnode, name)
{
    var children = hnode.children;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        var fieldType = getFieldType(child);
        if (fieldType == 'languageGrp' &&
            child.firstChild.children[1].innerText == name)
        {
            return child;
        }
    }

    return null;
}

InputModelValidator.prototype.findLanguageGrps = function (hnode)
{
    var result = new Array();

    var children = hnode.children;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        var fieldType = getFieldType(child);
        if (fieldType == 'languageGrp')
        {
            result.push(child);
        }
    }

    return result;
}

InputModelValidator.prototype.findTermGrps = function (hnode)
{
    var result = new Array();

    var children = hnode.children;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        var fieldType = getFieldType(child);
        if (fieldType == 'termGrp')
        {
            result.push(child);
        }
    }

    return result;
}

//
// Other helpers
//

InputModelValidator.prototype.trim = function (s)
{
    return s.replace(/^\s*|\s*$/g, "");
}

InputModelValidator.prototype.isEmptyField = function (hnode)
{
    var value = hnode.children[1].innerText;
    value = value.replace(/&nbsp;|&shy;|\u00a0/g, "");
    value = this.trim(value);

    return (value == '');
}

/**
 * Checks if an actual attribute value is defined in a comma-delimited
 * string of allowed values. "value" must be whitespace-trimmed.
 */
InputModelValidator.prototype.isValidAttributeValue = function (values, value)
{
    var vals = values.split(",");

    for (var i = 0; i < vals.length; i++)
    {
        var val = this.trim(vals[i]);

        if (val == value)
        {
            return true;
        }
    }

    return false;
}
