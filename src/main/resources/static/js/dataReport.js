// function showColumn() {
// }
// function showLinechart() {
// }
// function showPie() {
// }
// function showStackedbar() {
// }
/**
 *点击筛选按钮后进行一系列操作
 */
function dataReportFiltrate(){

    var start = document.getElementById("start").value;
    //console.log(start);
    var end = document.getElementById("end").value;
    // console.log(end);
    var dataset = '';
    var radio = document.getElementsByName("dataset");
    for (var i = 0; i < radio.length; i++) {
        if (radio[i].checked ) {
            dataset = radio[i].value + "," + dataset;
        }
    }
    dataset = dataset.substring(0, dataset.lastIndexOf(','));
    // console.log(dataset);
    var dimension=document.getElementsByName("dimensionality");
    for(var i=0; i<dimension.length; i++){
        if(dimension[i].checked){
            var dimensionality= dimension[i].value;
        }
    }
    // console.log(dimensionality);
    var top = document.getElementById("top").value;
    // console.log(top);

    var type=document.getElementsByName("chartType");
    for(var i=0; i<type.length; i++){
        if(type[i].checked){
           chartType=type[i].value
        }
    }
    // console.log(chartType);
    // $.ajax({
    //     url: '/query/tfidf',
    //     type: 'post',
    //     data: {
    //         "start": start.value.toString(),
    //         "end": end.value.toString(),
    //         "dataset": dataset.toString(),
    //     },
    //     success: function (data) {
    //         $("#body").empty();
    //         for (i in data) {
    //             var tr;
    //             var num = Number(i) + 1;
    //             tr = '<td>' + num + '</td>' +
    //                 '<td>' + data[i].word + '</td>' +
    //                 '<td>' + data[i].tf + '</td>' +
    //                 '<td>' + data[i].idf + '</td>' +
    //                 '<td>' + data[i].tfidf + '</td>'
    //             $("#body").append('<tr class=" ">' + tr + '</tr>')
    //         }
    //         $("#filenum").html(data.length);
    //     }
    // });
    var dataArray=new Array();
    var startdata=start;
    for(var i=0;i<parseInt(end-start)+1;i++){
        dataArray[i]=startdata;
        startdata++;
    }
    // console.log(dataArray)
        // 基于准备好的dom，初始化echarts实例
        var myChart = echarts.init(document.getElementById('main'));
        if(chartType=="折线图"){
            // 基于准备好的dom，初始化echarts实例
            // var myChart = echarts.init(document.getElementById('main'));
            // 指定图表的配置项和数据
            var option = {
                title: {
                    text: start+'-'+end+'年'+dimensionality+'TOP'+top+'的'+dataset+'数量及变化规律',
                    x:'center',
                    y: 'top',
                 //   textAlign:'center'
                },
                tooltip: {
                    show: true, // 是否显示
                    trigger: 'axis', // 触发类型，默认数据触发
                },
                legend: {
                    show:true,
                    top:"94%"
                },
                xAxis: {
                    name:"年份",
                    data:dataArray
                },
                yAxis: {
                    name:"频数"
                },
                series: [
                {
                    name: '晶圆制造',
                    type: 'line',
                    data: [5, 20, 36, 10, 10, 20]
                },
                {
                    name: '晶圆制造工艺制程',
                    type: 'line',
                    data: [15, 40, 66, 70, 20, 30]
                },
                {
                    name: '芯片制造',
                    type: 'line',
                    data: [25, 10, 60, 80, 20, 70]
                }
                ]
            };
            // 使用刚指定的配置项和数据显示图表。
            myChart.setOption(option);
        }else if(chartType=="柱状图"){
            // var myChart = echarts.init(document.getElementById('main'));
            var option = {
                title: {
                    text: start+'-'+end+'年'+dimensionality+'TOP'+top+'的'+dataset+'数量及变化规律',
                    x:'center',
                    y: 'top',
                 //   textAlign:'center'
                },
                tooltip: {
                    show: true, // 是否显示
                    trigger: 'axis', // 触发类型，默认数据触发
                },
                legend: {
                    show:true,
                    top:"94%"
                },
                xAxis: {
                    name:"年份",
                    data: dataArray
                },
                yAxis: {
                    name:"数量"
                },
                series: [
                {
                    name: '整合器件制造公司',
                    type: 'bar',
                    stack : false,
                    data: [5, 20, 36, 10, 10, 20],
                    itemStyle: {
                        normal: {
                            color: '#2f89cf', //柱子颜色
                            opacity: 1
                        }
                    }
                },
                {
                    name: '无生产线集成电路设计公司',
                    type: 'bar',
                    stack : false,
                    data: [15, 30, 66, 17, 14, 25],
                    itemStyle: {
                        normal: {
                            color: '#27d08a', //柱子颜色
                            opacity: 1
                        }
                    }

                },
                {
                    name: '中电科电子装备有限公司',
                    type: 'bar',
                    stack : false,
                    data: [45, 26, 16, 13, 18, 50]
                }
                ]
            };
            myChart.setOption(option);
        }else if(chartType=="饼图"){
            // var myChart = echarts.init(document.getElementById('main'));
            var option = {
                title: {
                    text: start+'-'+end+'年'+dimensionality+'TOP'+top+'的'+dataset+'数量及占比',
                    x:'center',
                    y: 'top',
                  //  textAlign:'center'
                },
                tooltip: {},
                legend: {
                    show:true,
                    top:"94%"
                },
                // xAxis: {
                //     show: false
                // },
                // yAxis: {
                //     show: false
                // },
                series: [{
                    // name: '销量',
                    type: 'pie',
                    radius:"85%",
                    data: [
                        {name:"美国",value:"152"},
                        {name:"中国",value:"154"},
                        {name:"日本",value:"142"},
                        {name:"韩国",value:"52"},
                        {name:"英国",value:"54"},
                        {name:"澳大利亚",value:"42"}
                    ]
                }]
            };
            myChart.setOption(option);
        }else{
            // var myChart = echarts.init(document.getElementById('main'));
            var option = {
                title: {
                    text: start+'-'+end+'年'+dimensionality+'TOP'+top+'的'+dataset+'数量及变化规律',
                    x:'center',
                    y: 'top',
                    //textAlign:'center'
                },
                tooltip: {
                    show: true, // 是否显示
                    trigger: 'axis', // 触发类型，默认数据触发
                },
                legend: {
                    show:true,
                    top:"94%"
                },
                xAxis: {
                    name:"年份",
                    data: dataArray
                },
                yAxis: {
                    name:"频数"
                },
                series: [
                    {
                        name: '集成电路制造技术的演进',
                        type: 'bar',
                        stack : true,
                        data: [150, 200, 360, 100, 100, 200],
                        itemStyle: {
                            normal: {
                                color: '#2f89cf', //柱子颜色
                            }
                        }
                    },
                    {
                        name: '集成电路中的硅基元件',
                        type: 'bar',
                        stack : true,
                        data: [100, 300, 660, 170, 140, 250],
                        itemStyle: {
                            normal: {
                                color: '#27d08a', //柱子颜色
                            }
                        }

                    },
                    {
                        name: '单项工艺',
                        type: 'bar',
                        stack : true,
                        data: [450, 260, 160, 130, 180, 500]
                    }
                ]
            };
            myChart.setOption(option);
        }

    }
