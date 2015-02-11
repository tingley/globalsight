var myScroll,element,
	pullDownEl, pullDownOffset,
	pullUpEl, pullUpOffset,
	generatedCount = 0;

var Scrollmanager={
	register:function(name,pullDownAction,pullUpAction){
		loaded();
		//document.addEventListener('touchmove', function (e) { e.preventDefault(); }, false);
		myScroll = new iScroll(name, wrapSroll);
		return myScroll;
	}
}


var wrapSroll= {
		checkDOMChanges: true,
		useTransition: true,
		topOffset: pullDownOffset,
		onRefresh: function () {
			if (pullDownEl.className.match('loading')) {
				pullDownEl.className = '';
			} else if (pullUpEl.className.match('loading')) {
				pullUpEl.className = '';
			}
		},
		onScrollMove: function () {
			if (this.y > 5 && !pullDownEl.className.match('flip')) {
				pullDownEl.className = 'flip';
				this.minScrollY = 0;
			} else if (this.y < 5 && pullDownEl.className.match('flip')) {
				this.minScrollY = -pullDownOffset;
			} else if (this.y < (this.maxScrollY - 5) && !pullUpEl.className.match('flip')) {
				pullUpEl.className = 'flip';
				this.maxScrollY = this.maxScrollY;
			} else if (this.y > (this.maxScrollY + 5) && pullUpEl.className.match('flip')) {
				this.maxScrollY = pullUpOffset;
			}
		},
		onScrollEnd: function () {
			if (pullDownEl.className.match('flip')) {
				pullDownEl.className = 'loading';
				pullDownAction();	// Execute custom function (ajax call?)
			} else if (pullUpEl.className.match('flip')) {
				pullUpEl.className = 'loading';
				pullUpAction();	// Execute custom function (ajax call?)
			}
		}
	}




// function pullDownAction () {
	// setTimeout(function () {	// <-- Simulate network congestion, remove setTimeout from production!
		// alert("pullDownAction")
// 		
		// myScroll.refresh();		// Remember to refresh when contents are loaded (ie: on ajax completion)
	// }, 1000);	// <-- Simulate network congestion, remove setTimeout from production!
// }
// 
// function pullUpAction () {
	// setTimeout(function () {	// <-- Simulate network congestion, remove setTimeout from production!
		// alert("pullUpAction")
// 		
		// myScroll.refresh();		// Remember to refresh when contents are loaded (ie: on ajax completion)
		// myScroll.scrollToElement('li:nth-child(1)', 2000);
		// //myScroll.scrollTo(0, 200, 2000);
		// //myScroll.scrollTo(200, 0, 2000);
	// }, 1000);	// <-- Simulate network congestion, remove setTimeout from production!
// }




function loaded() {
	
	pullDownEl = {};
	pullDownEl.className="";
	pullDownOffset = 5;
	pullUpEl = {};	
	pullUpEl.className="";
	pullUpOffset = 5;
	
}



