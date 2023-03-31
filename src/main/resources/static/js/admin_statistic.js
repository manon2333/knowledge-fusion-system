var entityType = getUrlParam('entityType'); //搜索类型
var id = getUrlParam('id'); //实体id
var latest = getUrlParam('latest'); //是否取最新版本的实体数据，默认为true
var version = getUrlParam('version'); //实体版本号，非必要参数。但如果 latest 为false，则这个参数必须为一个长整形数字，用于标识指定的实体副本

//前台搜索按钮
layui.use(['form','layer'], function(){
    var form = layui.form;
    var layer = layui.layer;
    form.render();

    //搜索 类型选项
    $.ajax({
        url:'/query/enttp',
        type:'post',
        //data:{type:entityType,kw:keyword,results:results,page:page},
        success:function(msg){
            if(msg.success){
                var data = JSON.parse(msg.json);
                // console.log(data);
                var str = '<option value="">不限</option>';
                for(var i=0; i<data.entityType.length; i++){
                    str += '<option value="' + data.entityType[i].name + '">' + data.entityType[i].name + '</option>';
                }
                $('.xzs').append(str);
                form.render('select');
            }
        },
        error:function(err){
            console.log(err);
        }
    });

    //搜索 监听提交
    form.on('submit(formDemo)', function(data){
        // layer.msg(JSON.stringify(data.field));
        var data = data.field;

        window.location = '/home/list.html?entityType=' + encodeURI(data.entityType) + '&keyword=' + encodeURI(data.keyword);
        return false;
    });

    /*
        历史版本弹出层
       */
    $('.lsbb').on('click',function(){
        layer.open({
            id:'lsbb',
            type: 2,
            title:'编辑历史',
            area: ['1000px', '550px'],
            content: './menuOpen/lsbb.html', //这里content是一个URL，如果你不想让iframe出现滚动条，你还可以content: ['http://sentsin.com', 'no']
            success: function(layero, index){
                var body = layer.getChildFrame('body', index);
                var iframeWin = window[layero.find('iframe')[0]['name']]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
                iframeWin.child(entityType,id);
            }
        });
    });

});
//获取地址栏参数
function getUrlParam(name) {
    //构造一个含有目标参数的正则表达式对象
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    //匹配目标参数
    var r = window.location.search.substr(1).match(reg);
    //返回参数
    if (r != null) {
        // return unescape(r[2]);
        return decodeURI(r[2]);
    } else {
        return null;
    }
}
//时间戳 转 字符串 1995-12-05 00:00:00
function getTime(str){
    if(str == ''){
        return '';
    }
    var oDate = new Date(str),
        oYear = oDate.getFullYear(),
        oMonth = oDate.getMonth()+1,
        oDay = oDate.getDate(),
        oHour = oDate.getHours(),
        oMin = oDate.getMinutes(),
        oSen = oDate.getSeconds(),
        // oTime = oYear +'-'+ getzf(oMonth) +'-'+ getzf(oDay) +' '+ getzf(oHour) +':'+ getzf(oMin) +':'+getzf(oSen);//最后拼接时间
        oTime = oYear +'-'+ getzf(oMonth) +'-'+ getzf(oDay);//最后拼接时间
    return oTime;

}
//补0操作
function getzf(num){
    if(parseInt(num) < 10){
        num = '0'+num;
    }
    return num;
}
//详情页 url 跳转字符换拼接
function comUrl(entityType,id,latest,version){
    var str = '?id=' + id + '&latest=' + latest + '&version=' + version + '&entityType=' + entityType;
    return str;
}

// 关联实体 遍历渲染 方法
function showGlst(data){
    if(data.brothersEntityListA){
        $('.queList1').show();
        var str2 = '';
        var dtl2 = data.brothersEntityListA;
        $('.queList1 .entType').text(dtl2[0].entityType);
        for(var i=0; i<dtl2.length; i++){
            var imgSrc = dtl2[i].picture ? dtl2[i].picture : '/images/img404.png' ;
            str2 += '<li title="' + dtl2[i].name + '"><a href="/home/content.html?id=' + dtl2[i].id + '&latest=' + dtl2[i].latest + '&version=' + dtl2[i].version + '&entityType=' + dtl2[i].entityType + '" target="_blank">' +
                '<img src="' + imgSrc + '" alt="' + dtl2[i].name + '">' +
                '<h6>' + dtl2[i].name + '</h6>' +
                '</a></li>';
        }
        $('.queList1 .cbl_ul').html(str2);
    }else{
        $('.queList1').hide();
    }

    if(data.brothersEntityListB){
        $('.queList2').show();
        var str3 = '';
        var dtl3 = data.brothersEntityListB;
        $('.queList2 .entType').text(dtl3[0].entityType);
        for(var i=0; i<dtl3.length; i++){
            var imgSrc = dtl3[i].picture ? dtl3[i].picture : '/images/img404.png' ;
            str3 += '<li title="' + dtl2[i].name + '"><a href="/home/content.html?id=' + dtl3[i].id + '&latest=' + dtl3[i].latest + '&version=' + dtl3[i].version + '&entityType=' + dtl3[i].entityType + '" target="_blank">' +
                '<img src="' + imgSrc + '" alt="' + dtl3[i].name + '">' +
                '<h6>' + dtl3[i].name + '</h6>' +
                '</a></li>';
        }
        $('.queList2 .cbl_ul').html(str3);
    }else{
        $('.queList2').hide();
    }
}
//详情页 实体详情  图模型 树模型 跳转方法
function tz(str){

    var u = '?id=' + id + '&latest=' + latest + '&version=' + version + '&entityType=' + encodeURI(entityType);
    var url = '';
    if(str == 'st'){
        url = "/home/content.html" + u;
    }else if(str == 't'){
        url = "/home/content_02.html" + u;
    }else if(str == 's'){
        url = "/home/content_03.html" + u;
    }
    console.log(url);
    location.href = url;
}

//详情页 获取实体属性
function getSt(entityType,id,latest,version){

    $.ajax({
        url:'/query/entid',
        type:'post',
        data:{entityType:entityType,id:id,latest:latest,version:version},
        success:function(msg){
            if(msg.success){
                var data = JSON.parse(msg.json);
                if(!data.verified){
                    $('.xd').text('未修订').removeClass('fa');
                }else{
                    $('.xd').text('已修订').addClass('fa');
                }

                //标题 及 属性标志
                $('.sub h3').html('<b class="logo-01">' + data.entity.entityType.charAt(data.entity.entityType.length-1) + '</b>' + data.entity.name);
                //摘要
                $('.sub .content').text(data.entity.description);

                var properties = data.entity.properties;
                var relations = data.entity.relations;
                var imgDt = data.entity.picture ? data.entity.picture : '/images/img404.png' ;
                $('.imgDt').prop('src',imgDt);
                var str1 = '';
                for(var i=0; i<properties.length; i++){

                    str1 += '<li>' +
                        '  <span class="lis1">' + properties[i].name + '</span>' +
                        '  <span class="lis2">' + properties[i].value + '</span>' +
                        '  <span class="lis3"><a href="javascript:;">' + properties[i].src.prefix + '</a></span>' +
                        '</li>';
                }

                $('.properties').html(str1);

                var str2 = '';
                for(var i=0; i<relations.length; i++){

                    str2 += '<li>' +
                        '  <span class="lis1">' + relations[i].endName + '</span>' +
                        '  <span class="lis2">' + relations[i].relType + '</span>' +
                        '  <span class="lis3"><a href="javascript:;">' + relations[i].dataSource.prefix + '</a></span>' +
                        '</li>';
                }

                $('.relations').html(str2);

                showGlst(data);
            }else{
                layer.msg(msg.errMsg);
            }
        },
        error:function(err){
            console.log(err);
        }
    });
}