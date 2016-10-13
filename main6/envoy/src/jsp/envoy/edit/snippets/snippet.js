  // need to add reference count
  function Snippet(name, desc, locale, displayLocale, id, value)
  {
    this.name = name;
    this.desc = desc;
    this.locale = locale;
    this.displayLocale = displayLocale;
    this.id = id;
    this.value = value;
  }

  function Snippet.prototype.clone()
  {
    return new Snippet(this.name, this.desc, this.locale, this.displayLocale, this.id, this.value);
  }

  function Snippet.prototype.toString()
  {
    return "Snippet " + this.name + " " + this.locale + " id=" + this.id;
  }

  function Snippet.prototype.isGeneric()
  {
    return (this.locale == null || this.locale == "");
  }

