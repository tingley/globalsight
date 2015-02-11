
function drag(o,s)  
{  
    if (typeof o == "string") o = document.getElementById(o); 
    
    var flag = false ; 
    o.orig_index = o.style.zIndex; 
    o.style.cursor = "move";   
    
    function down(a){
        this.style.cursor = "move";  
        this.style.zIndex = 10000;  

        if(!a)a=window.event;
        
        if(!flag) {
					if(o.setCapture)
						o.setCapture();
					else {
						document.addEventListener('mouseup', up, true);
						document.addEventListener('mousemove', move, true);
						a.preventDefault();
					}

					flag = true ;
				}
    }
    
    function up(a){
				if(flag){
				    if(o.releaseCapture)
						o.releaseCapture();
				else {
						document.removeEventListener('mouseup', up, true);
						document.removeEventListener('mousemove', move, true);
						a.preventDefault();
				}

					  flag = false ;
				}
		}
			
		function move(a){
				if(flag){
				    if(!a)a = window.event; 
				     
            o.style.left = a.clientX + document.body.scrollLeft ;

            o.orig_x = parseInt(o.style.left) - document.body.scrollLeft;  
            o.orig_y = parseInt(o.style.top) - document.body.scrollTop; 
        
            commonPositionChange();
				 
					  if(!o.releaseCapture) {
						    a.preventDefault();
					}
				}
		 }

    o.onmousedown =down;
    o.onmousemove = move;
    o.onmouseup = up;
} 
