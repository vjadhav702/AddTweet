var donut = document.getElementById('donut');
var donutCtx = donut.getContext('2d');
var data1 = [{ value: 2, color: '#76a346' }, { value: 1, color: '#FF0000' }];
data1[0]["value"]=4;
var chart = new Chart(donutCtx).Doughnut(data1, { percentageInnerCutout: 50, animateScale: true, segmentShowStroke: false});

