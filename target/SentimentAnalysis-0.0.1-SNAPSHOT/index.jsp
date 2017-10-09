<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>SAP Composite Demo</title>

<link rel='stylesheet prefetch' href='https://netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.min.css'>
<link rel="stylesheet" href="css/style.css">
<script src='https://cdnjs.cloudflare.com/ajax/libs/Chart.js/0.2.0/Chart.min.js'></script>
<script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>

<script>
	$(document).ready(function() {
		
		$.ajax({
			url : 'GetSentiments',
			data : {
				userName : "create:"
			},
			success : function(responseText) {
				if (responseText === 'Error') {
					jQuery("body").html("There is some problem, Try again in some time");
				} else {
    				var values = responseText.split(":");
    				var pos = parseInt(values[0]);
    				var neg = parseInt(values[1]);
    				
    				var total = pos+neg;
    				
    				var posPer = (pos/total)*100;
    				var negPer = (neg/total)*100;
    				
    				$('#positiveSpan').text(pos);
    				$('#negativeSpan').text(neg);
    				$('#positiveSpanPer').text(posPer+'%');
    				$('#negativeSpanPer').text(negPer+'%');
    				
    				
    				var donut = document.getElementById('donut');
    				var donutCtx = donut.getContext('2d');
    				var data1 = [{ value: 2, color: '#76a346' }, { value: 1, color: '#FF0000' }];
    				data1[0]["value"]=parseInt(pos);
    				data1[1]["value"]=parseInt(neg);
    				var chart = new Chart(donutCtx).Doughnut(
    						data1, 
    						{ percentageInnerCutout: 50, animateScale: true, segmentShowStroke: false}
    						);
					}
			},
			error: function(responseText) { 
		        alert("Error is there ...."); 
		    }  
		});
	});
	
</script>
</head>
<body>
<div class='widget widget--2x1'>
  <div class='widget-content'>
    <canvas id='donut' width='400px' ></canvas>
    <ul class='value-list'>
      <li>
        <div class='description'>
          Positive Tweets
        </div>
        <div class='value'>
          <span id= 'positiveSpan'></span>
          <span id= 'positiveSpanPer' class='positive small'>
            
          </span>
        </div>
      </li>
      <li>
        <div class='description'>
          Negative Tweets
        </div>
        <div class='value'>
          <span id='negativeSpan'></span>
          <span id='negativeSpanPer' class='negative small'>
            
          </span>
        </div>
      </li>
    </ul>
  </div>
  </div>
</body>
</html>