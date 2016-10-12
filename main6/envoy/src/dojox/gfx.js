/*
	Copyright (c) 2004-2009, The Dojo Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

/*
	This is a compiled version of Dojo, built for deployment and not for
	development. To get an editable version, please visit:

		http://dojotoolkit.org

	for documentation and information on getting the source.
*/

if(!dojo._hasResource["dojox.gfx.matrix"]){dojo._hasResource["dojox.gfx.matrix"]=true;dojo.provide("dojox.gfx.matrix");(function(){var m=dojox.gfx.matrix;var _1={};m._degToRad=function(_2){return _1[_2]||(_1[_2]=(Math.PI*_2/180));};m._radToDeg=function(_3){return _3/Math.PI*180;};m.Matrix2D=function(_4){if(_4){if(typeof _4=="number"){this.xx=this.yy=_4;}else{if(_4 instanceof Array){if(_4.length>0){var _5=m.normalize(_4[0]);for(var i=1;i<_4.length;++i){var l=_5,r=dojox.gfx.matrix.normalize(_4[i]);_5=new m.Matrix2D();_5.xx=l.xx*r.xx+l.xy*r.yx;_5.xy=l.xx*r.xy+l.xy*r.yy;_5.yx=l.yx*r.xx+l.yy*r.yx;_5.yy=l.yx*r.xy+l.yy*r.yy;_5.dx=l.xx*r.dx+l.xy*r.dy+l.dx;_5.dy=l.yx*r.dx+l.yy*r.dy+l.dy;}dojo.mixin(this,_5);}}else{dojo.mixin(this,_4);}}}};dojo.extend(m.Matrix2D,{xx:1,xy:0,yx:0,yy:1,dx:0,dy:0});dojo.mixin(m,{identity:new m.Matrix2D(),flipX:new m.Matrix2D({xx:-1}),flipY:new m.Matrix2D({yy:-1}),flipXY:new m.Matrix2D({xx:-1,yy:-1}),translate:function(a,b){if(arguments.length>1){return new m.Matrix2D({dx:a,dy:b});}return new m.Matrix2D({dx:a.x,dy:a.y});},scale:function(a,b){if(arguments.length>1){return new m.Matrix2D({xx:a,yy:b});}if(typeof a=="number"){return new m.Matrix2D({xx:a,yy:a});}return new m.Matrix2D({xx:a.x,yy:a.y});},rotate:function(_6){var c=Math.cos(_6);var s=Math.sin(_6);return new m.Matrix2D({xx:c,xy:-s,yx:s,yy:c});},rotateg:function(_7){return m.rotate(m._degToRad(_7));},skewX:function(_8){return new m.Matrix2D({xy:Math.tan(_8)});},skewXg:function(_9){return m.skewX(m._degToRad(_9));},skewY:function(_a){return new m.Matrix2D({yx:Math.tan(_a)});},skewYg:function(_b){return m.skewY(m._degToRad(_b));},reflect:function(a,b){if(arguments.length==1){b=a.y;a=a.x;}var a2=a*a,b2=b*b,n2=a2+b2,xy=2*a*b/n2;return new m.Matrix2D({xx:2*a2/n2-1,xy:xy,yx:xy,yy:2*b2/n2-1});},project:function(a,b){if(arguments.length==1){b=a.y;a=a.x;}var a2=a*a,b2=b*b,n2=a2+b2,xy=a*b/n2;return new m.Matrix2D({xx:a2/n2,xy:xy,yx:xy,yy:b2/n2});},normalize:function(_c){return (_c instanceof m.Matrix2D)?_c:new m.Matrix2D(_c);},clone:function(_d){var _e=new m.Matrix2D();for(var i in _d){if(typeof (_d[i])=="number"&&typeof (_e[i])=="number"&&_e[i]!=_d[i]){_e[i]=_d[i];}}return _e;},invert:function(_f){var M=m.normalize(_f),D=M.xx*M.yy-M.xy*M.yx,M=new m.Matrix2D({xx:M.yy/D,xy:-M.xy/D,yx:-M.yx/D,yy:M.xx/D,dx:(M.xy*M.dy-M.yy*M.dx)/D,dy:(M.yx*M.dx-M.xx*M.dy)/D});return M;},_multiplyPoint:function(_10,x,y){return {x:_10.xx*x+_10.xy*y+_10.dx,y:_10.yx*x+_10.yy*y+_10.dy};},multiplyPoint:function(_11,a,b){var M=m.normalize(_11);if(typeof a=="number"&&typeof b=="number"){return m._multiplyPoint(M,a,b);}return m._multiplyPoint(M,a.x,a.y);},multiply:function(_12){var M=m.normalize(_12);for(var i=1;i<arguments.length;++i){var l=M,r=m.normalize(arguments[i]);M=new m.Matrix2D();M.xx=l.xx*r.xx+l.xy*r.yx;M.xy=l.xx*r.xy+l.xy*r.yy;M.yx=l.yx*r.xx+l.yy*r.yx;M.yy=l.yx*r.xy+l.yy*r.yy;M.dx=l.xx*r.dx+l.xy*r.dy+l.dx;M.dy=l.yx*r.dx+l.yy*r.dy+l.dy;}return M;},_sandwich:function(_13,x,y){return m.multiply(m.translate(x,y),_13,m.translate(-x,-y));},scaleAt:function(a,b,c,d){switch(arguments.length){case 4:return m._sandwich(m.scale(a,b),c,d);case 3:if(typeof c=="number"){return m._sandwich(m.scale(a),b,c);}return m._sandwich(m.scale(a,b),c.x,c.y);}return m._sandwich(m.scale(a),b.x,b.y);},rotateAt:function(_14,a,b){if(arguments.length>2){return m._sandwich(m.rotate(_14),a,b);}return m._sandwich(m.rotate(_14),a.x,a.y);},rotategAt:function(_15,a,b){if(arguments.length>2){return m._sandwich(m.rotateg(_15),a,b);}return m._sandwich(m.rotateg(_15),a.x,a.y);},skewXAt:function(_16,a,b){if(arguments.length>2){return m._sandwich(m.skewX(_16),a,b);}return m._sandwich(m.skewX(_16),a.x,a.y);},skewXgAt:function(_17,a,b){if(arguments.length>2){return m._sandwich(m.skewXg(_17),a,b);}return m._sandwich(m.skewXg(_17),a.x,a.y);},skewYAt:function(_18,a,b){if(arguments.length>2){return m._sandwich(m.skewY(_18),a,b);}return m._sandwich(m.skewY(_18),a.x,a.y);},skewYgAt:function(_19,a,b){if(arguments.length>2){return m._sandwich(m.skewYg(_19),a,b);}return m._sandwich(m.skewYg(_19),a.x,a.y);}});})();dojox.gfx.Matrix2D=dojox.gfx.matrix.Matrix2D;}if(!dojo._hasResource["dojox.gfx._base"]){dojo._hasResource["dojox.gfx._base"]=true;dojo.provide("dojox.gfx._base");(function(){var g=dojox.gfx,b=g._base;g._hasClass=function(_1a,_1b){var cls=_1a.getAttribute("className");return cls&&(" "+cls+" ").indexOf(" "+_1b+" ")>=0;};g._addClass=function(_1c,_1d){var cls=_1c.getAttribute("className")||"";if(!cls||(" "+cls+" ").indexOf(" "+_1d+" ")<0){_1c.setAttribute("className",cls+(cls?" ":"")+_1d);}};g._removeClass=function(_1e,_1f){var cls=_1e.getAttribute("className");if(cls){_1e.setAttribute("className",cls.replace(new RegExp("(^|\\s+)"+_1f+"(\\s+|$)"),"$1$2"));}};b._getFontMeasurements=function(){var _20={"1em":0,"1ex":0,"100%":0,"12pt":0,"16px":0,"xx-small":0,"x-small":0,"small":0,"medium":0,"large":0,"x-large":0,"xx-large":0};if(dojo.isIE){dojo.doc.documentElement.style.fontSize="100%";}var div=dojo.doc.createElement("div");var s=div.style;s.position="absolute";s.left="-100px";s.top="0px";s.width="30px";s.height="1000em";s.border="0px";s.margin="0px";s.padding="0px";s.outline="none";s.lineHeight="1";s.overflow="hidden";dojo.body().appendChild(div);for(var p in _20){div.style.fontSize=p;_20[p]=Math.round(div.offsetHeight*12/16)*16/12/1000;}dojo.body().removeChild(div);div=null;return _20;};var _21=null;b._getCachedFontMeasurements=function(_22){if(_22||!_21){_21=b._getFontMeasurements();}return _21;};var _23=null,_24={};b._getTextBox=function(_25,_26,_27){var m,s,al=arguments.length;if(!_23){m=_23=dojo.doc.createElement("div");s=m.style;s.position="absolute";s.left="-10000px";s.top="0";dojo.body().appendChild(m);}else{m=_23;s=m.style;}m.className="";s.border="0";s.margin="0";s.padding="0";s.outline="0";if(al>1&&_26){for(var i in _26){if(i in _24){continue;}s[i]=_26[i];}}if(al>2&&_27){m.className=_27;}m.innerHTML=_25;if(m["getBoundingClientRect"]){var bcr=m.getBoundingClientRect();return {l:bcr.left,t:bcr.top,w:bcr.width||(bcr.right-bcr.left),h:bcr.height||(bcr.bottom-bcr.top)};}else{return dojo.marginBox(m);}};var _28=0;b._getUniqueId=function(){var id;do{id=dojo._scopeName+"Unique"+(++_28);}while(dojo.byId(id));return id;};})();dojo.mixin(dojox.gfx,{defaultPath:{type:"path",path:""},defaultPolyline:{type:"polyline",points:[]},defaultRect:{type:"rect",x:0,y:0,width:100,height:100,r:0},defaultEllipse:{type:"ellipse",cx:0,cy:0,rx:200,ry:100},defaultCircle:{type:"circle",cx:0,cy:0,r:100},defaultLine:{type:"line",x1:0,y1:0,x2:100,y2:100},defaultImage:{type:"image",x:0,y:0,width:0,height:0,src:""},defaultText:{type:"text",x:0,y:0,text:"",align:"start",decoration:"none",rotated:false,kerning:true},defaultTextPath:{type:"textpath",text:"",align:"start",decoration:"none",rotated:false,kerning:true},defaultStroke:{type:"stroke",color:"black",style:"solid",width:1,cap:"butt",join:4},defaultLinearGradient:{type:"linear",x1:0,y1:0,x2:100,y2:100,colors:[{offset:0,color:"black"},{offset:1,color:"white"}]},defaultRadialGradient:{type:"radial",cx:0,cy:0,r:100,colors:[{offset:0,color:"black"},{offset:1,color:"white"}]},defaultPattern:{type:"pattern",x:0,y:0,width:0,height:0,src:""},defaultFont:{type:"font",style:"normal",variant:"normal",weight:"normal",size:"10pt",family:"serif"},getDefault:(function(){var _29={};return function(_2a){var t=_29[_2a];if(t){return new t();}t=_29[_2a]=new Function;t.prototype=dojox.gfx["default"+_2a];return new t();};})(),normalizeColor:function(_2b){return (_2b instanceof dojo.Color)?_2b:new dojo.Color(_2b);},normalizeParameters:function(_2c,_2d){if(_2d){var _2e={};for(var x in _2c){if(x in _2d&&!(x in _2e)){_2c[x]=_2d[x];}}}return _2c;},makeParameters:function(_2f,_30){if(!_30){return dojo.delegate(_2f);}var _31={};for(var i in _2f){if(!(i in _31)){_31[i]=dojo.clone((i in _30)?_30[i]:_2f[i]);}}return _31;},formatNumber:function(x,_32){var val=x.toString();if(val.indexOf("e")>=0){val=x.toFixed(4);}else{var _33=val.indexOf(".");if(_33>=0&&val.length-_33>5){val=x.toFixed(4);}}if(x<0){return val;}return _32?" "+val:val;},makeFontString:function(_34){return _34.style+" "+_34.variant+" "+_34.weight+" "+_34.size+" "+_34.family;},splitFontString:function(str){var _35=dojox.gfx.getDefault("Font");var t=str.split(/\s+/);do{if(t.length<5){break;}_35.style=t[0];_35.variant=t[1];_35.weight=t[2];var i=t[3].indexOf("/");_35.size=i<0?t[3]:t[3].substring(0,i);var j=4;if(i<0){if(t[4]=="/"){j=6;}else{if(t[4].charAt(0)=="/"){j=5;}}}if(j<t.length){_35.family=t.slice(j).join(" ");}}while(false);return _35;},cm_in_pt:72/2.54,mm_in_pt:7.2/2.54,px_in_pt:function(){return dojox.gfx._base._getCachedFontMeasurements()["12pt"]/12;},pt2px:function(len){return len*dojox.gfx.px_in_pt();},px2pt:function(len){return len/dojox.gfx.px_in_pt();},normalizedLength:function(len){if(len.length==0){return 0;}if(len.length>2){var _36=dojox.gfx.px_in_pt();var val=parseFloat(len);switch(len.slice(-2)){case "px":return val;case "pt":return val*_36;case "in":return val*72*_36;case "pc":return val*12*_36;case "mm":return val*dojox.gfx.mm_in_pt*_36;case "cm":return val*dojox.gfx.cm_in_pt*_36;}}return parseFloat(len);},pathVmlRegExp:/([A-Za-z]+)|(\d+(\.\d+)?)|(\.\d+)|(-\d+(\.\d+)?)|(-\.\d+)/g,pathSvgRegExp:/([A-Za-z])|(\d+(\.\d+)?)|(\.\d+)|(-\d+(\.\d+)?)|(-\.\d+)/g,equalSources:function(a,b){return a&&b&&a==b;}});}if(!dojo._hasResource["dojox.gfx"]){dojo._hasResource["dojox.gfx"]=true;dojo.provide("dojox.gfx");dojo.loadInit(function(){var gfx=dojo.getObject("dojox.gfx",true),sl,_37,_38;if(!gfx.renderer){if(dojo.config.forceGfxRenderer){dojox.gfx.renderer=dojo.config.forceGfxRenderer;return;}var _39=(typeof dojo.config.gfxRenderer=="string"?dojo.config.gfxRenderer:"svg,vml,silverlight,canvas").split(",");var ua=navigator.userAgent,_3a=0,_3b=0;if(dojo.isSafari>=3){if(ua.indexOf("iPhone")>=0||ua.indexOf("iPod")>=0){_38=ua.match(/Version\/(\d(\.\d)?(\.\d)?)\sMobile\/([^\s]*)\s?/);if(_38){_3a=parseInt(_38[4].substr(0,3),16);}}}if(dojo.isWebKit){if(!_3a){_38=ua.match(/Android\s+(\d+\.\d+)/);if(_38){_3b=parseFloat(_38[1]);}}}for(var i=0;i<_39.length;++i){switch(_39[i]){case "svg":if(!dojo.isIE&&(!_3a||_3a>=1521)&&!_3b&&!dojo.isAIR){dojox.gfx.renderer="svg";}break;case "vml":if(dojo.isIE){dojox.gfx.renderer="vml";}break;case "silverlight":try{if(dojo.isIE){sl=new ActiveXObject("AgControl.AgControl");if(sl&&sl.IsVersionSupported("1.0")){_37=true;}}else{if(navigator.plugins["Silverlight Plug-In"]){_37=true;}}}catch(e){_37=false;}finally{sl=null;}if(_37){dojox.gfx.renderer="silverlight";}break;case "canvas":if(!dojo.isIE){dojox.gfx.renderer="canvas";}break;}if(dojox.gfx.renderer){break;}}if(dojo.config.isDebug){}}});dojo.requireIf(dojox.gfx.renderer=="svg","dojox.gfx.svg");dojo.requireIf(dojox.gfx.renderer=="vml","dojox.gfx.vml");dojo.requireIf(dojox.gfx.renderer=="silverlight","dojox.gfx.silverlight");dojo.requireIf(dojox.gfx.renderer=="canvas","dojox.gfx.canvas");}
