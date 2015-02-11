
function lmcard( cid )
{
	lmswith( cid , 'card' );
}

function lmtab( tid )
{
	lmswith( tid , 'tab' );
}

function lmswith( id , name )
{
	$(".LM-"+name).each( function( index , value )
	{
		if($(this).attr('id') == id )
		{
			$(this).addClass('cur');
		}
		else
		{
			$(this).removeClass('cur');
		}
	});

}

function lmlist( lid , tpl , iscroll , nocache )
{
	var lc = new LMconnector(); 
	if( nocache == 1 ) lc.cache = false;
	
	lc.connect('http://error.sinaapp.com/demo.php' , {} , function( data )
	{
		// render it
		$('#'+tpl).tmpl(data).appendTo( "#"+lid );
		$("#"+lid).listview('refresh');
		iscroll.refresh();
		
		// show remote if first load will local data
		if( lc.method == 'local' )
		{
			//lmlist( lid , tpl , iscroll , 1 );
			//(function(){}).delay(1000);
		}
		
	});
		
}