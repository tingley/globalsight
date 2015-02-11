
Array.prototype.index = function (el) {  
    var i = 0;  
    for (var i = 0, len = this.length; i < len; i++) {  
        if (el == this[i]) {  
            return i;  
        }  
    }  
    return -1;    
};  
  
Array.prototype.same = function () {     
    if (this.length == 0) {  
        return true;  
    }  
    var temp = this.join('').replaceAll(this[0], "");     
    if (temp != "") {     
        return false;     
    } else {     
        return true    
    }     
};                

Array.prototype.iterate = function () {  
    var flag = false;  
    for (var i = 0, len = this.length; i < len; i++) {  
        for (var j = 0; j < len; j++) {  
            if (this[i] == this[j] && i != j) {  
                flag = true;  
                break;  
            }  
        }  
    }  
    return flag;  
};  
  
Array.prototype.filter = function(callback) {  
    var rs = [];      
    for ( var i = 0, length = this.length; i < length; i++ ) {  
        if (callback(this[i], i)) {  
            rs.push(this[i]);  
        }  
    }  
    return rs;  
};

Array.prototype.remove = function(oneItem)
{
	var index = this.index(oneItem);
	
	if (index != -1)
	{
		return this.splice(index, 1);
	}
	
	return this;
}
