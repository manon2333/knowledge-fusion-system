/**
 * laydateTab v1.0
 * @Name：多个laydate选择框, 依赖laydate
 * @Author：xumi
 * MIT License
 */
;! function() {
	"use strict";
	var isLayui = window.layui && layui.define,
	ready = {
		getPath: function() {
			var jsPath = document.currentScript ? document.currentScript.src : function() {
				var js = document.scripts,
					last = js.length - 1,
					src;
				for (var i = last; i > 0; i--) {
					if (js[i].readyState === 'interactive') {
						src = js[i].src;
						break;
					}
				}
				return src || js[last].src;
			}();
			return jsPath.substring(0, jsPath.lastIndexOf('/') + 1);
		}(),
		//获取节点的style属性值
		getStyle: function(node, name) {
			var style = node.currentStyle ? node.currentStyle : window.getComputedStyle(node, null);
			return style[style.getPropertyValue ? 'getPropertyValue' : 'getAttribute'](name);
		},
		//载入CSS配件
		link: function(href, fn, cssname) {
			//未设置路径，则不主动加载css
			if (!laydateTab.path) return;

			var head = document.getElementsByTagName("head")[0],
				link = document.createElement('link');
			if (typeof fn === 'string') cssname = fn;
			var app = (cssname || href).replace(/\.|\//g, '');
			var id = 'layuicss-' + app,
				timeout = 0;

			link.rel = 'stylesheet';
			link.href = laydateTab.path + href;
			link.id = id;

			if (!document.getElementById(id)) {
				head.appendChild(link);
			}

			if (typeof fn !== 'function') return;

			//轮询css是否加载完毕
			(function poll() {
				if (++timeout > 8 * 1000 / 100) {
					return window.console && console.error(id + '.css: Invalid');
				};
				parseInt(ready.getStyle(document.getElementById(id), 'width')) === 1989 ? fn() : setTimeout(poll, 100);
			}());
		}
	};

	// 真正实现
	var lay,
	laydate,
	laydateTab = {
		v: '1.0',
		config: {},
		path: ready.getPath,
		//主体CSS等待事件
		ready: function(fn) {
			lay = window.lay;
			laydate = laydate ? laydate : (isLayui ? layui.laydate : window.laydate);
			var cssname = 'laydateTab',
			path = 'css/laydateTab.css?v=' + laydateTab.v;
			ready.link(path, fn, cssname);
			return this;
		},
		// 加载css
		link: function(path, fn, cssname) {
			ready.link(path, fn, cssname);
		}
	},
	// 常量
	MOD_NAME = 'laydateTab',
	INDEX = 0,
	MAIN_CLASS = 'laydate-tab',
	TITLE_CLASS = 'laydate-tab-title',
	PANL_CLASS = 'laydate-tab-panl',
	SELECTED_CLASS = 'selected',
	SELECTED_CSS = '.laydate-tab>ul>li.selected',
	FORMAT_REG_EXP = 'yyyy|y|MM|M|dd|d|HH|H|mm|m|ss|s',
	// 替换格式，避免提示错误
	FOAMAT_REPLACE_REG = {
		year: /(M{1,2}|d{1,2}|H{1,2}|m{1,2}|s{1,2})[^y]/g,
		month: /(d{1,2}|H{1,2}|m{1,2}|s{1,2})[^M]/g,
		date: /(H{1,2}|m{1,2}|s{1,2})[^d]/g,
		time: /(y{2,4}|M{1,2}|d{1,2})[^H]/g
	},
	FORMAT = {
		year: 'yyyy',
		month: 'yyyy-MM',
		date: 'yyyy-MM-dd',
		time: 'HH:mm:ss',
		datetime: 'yyyy-MM-dd HH:mm:ss'
	},
	// 快捷方式
	TAB_FAST_TYPE = {
		ymd: ['year', 'month', 'date'], // 默认
		ym: ['year', 'month'],
		md: ['month', 'date'],
		all: ['year', 'month', 'date', 'time', 'datetime']
	},
	// 默认tab页名称
	TAB_DEFUALT_TITLE = {
		year: '年',
		month: '月',
		date: '日期',
		datetime: '日期时间',
		time: '时间'
	},
	// 值转换成数组
	valueToArray = function(value) {
		return value.constructor === Array ? value : [value];
	},
	// 从数组中获取值
	getValueByArray = function(arrs, i) {
		return typeof arrs[i] !== 'undefined' ? arrs[i] : arrs[0];
	},
	// 实例对象
	Class = function(options) {
		if (!laydate) {
			console.error('没有找到layui中laydate组件，无法进行渲染！');
			return;
		}
		if (!options.elem) {
			console.error('没有绑定相关的触发元素！');
			return;
		};
		this.config = this.getDefualtConfig();
		// 覆盖相关的数组值
		for(var key in this.config) {
			if (options[key] && this.config[key].constructor === Array) {
				delete this.config[key];
			}
		}
		this.config = lay.extend({}, this.config, options);
		// btns 特殊处理
		
		options.btns && (this.config.btns = typeof options.btns[0] === 'string' ? [options.btns] : options.btns);
		
		this.elem = lay(options.elem);
		this._id = MOD_NAME + INDEX++;
		// 定制皮肤#颜色值
		if(/^#/.test(options.theme)){
		  var style = lay.elem('style')
		  ,styleText = '#' + this._id + SELECTED_CSS + '{background: '+ options.theme + '}';
		  if('styleSheet' in style){
			style.setAttribute('type', 'text/css');
		    style.styleSheet.cssText = styleText;
		  } else {
		    style.innerHTML = styleText;
		  }
		  lay('body').append(style);
		}
		// 如果值为字符串则应该初始化值给elem
		typeof options.value === 'string' && (this.val(options.value), delete this.config.value);
		this.bindEvent();
	};

	// 默认配置
	Class.prototype.getDefualtConfig = function(){
		return {
			trigger: 'click', // 触发显示的事件
			type: TAB_FAST_TYPE.ymd // 默认显示年月日
		};
	}

	// 渲染
	Class.prototype.render = function() {
		this.creteTab();
	}

	// 创建相关的元素
	Class.prototype.creteTab = function() {
		var options = this.config,
		targetElem = this.elem,
		tabElem = lay(lay.elem('div', {'class': MAIN_CLASS, id: this._id})),
		titleElem = this.titleElem = lay(lay.elem('ul', {'class': TITLE_CLASS})),
		panlElem = this.panlElem = lay(lay.elem('div', {'class': PANL_CLASS})),
		// 解析配置
		types = this.types = this.getTypes(options.type),
		typeLen = types.length,
		elemValue = this.isInput(targetElem[0]) ? targetElem[0].value : targetElem[0].innerHTML,
		// 默认选择项
		selectedType = options.selected ? options.selected : types[0],
		selectedType = TAB_DEFUALT_TITLE[selectedType] ? selectedType : types[0],
		selectIndex = null;
		this.tabElem = tabElem;
		delete options.type;
		this.laydateObj = [];
		this.laydateOptions = [];

		for (var i = 0; i < typeLen; i++) {
			var option = {},
			type = option.type = types[i],
			selectedClass = type === selectedType ? {'class' : SELECTED_CLASS} : null,
			laytitleContainer = lay(lay.elem('li', selectedClass)),
			laydateContainer = lay.elem('div', selectedClass);

			laytitleContainer[0].index = i;
			for (var key in options) {
				var value = options[key],
				value = valueToArray(value),
				_thatValue = getValueByArray(value, i);
				_thatValue && (option[key] = _thatValue);
			}
			laytitleContainer.html(option.title ? option.title : TAB_DEFUALT_TITLE[type]);
			option.done = this.doneFn(option.done);
			option.position = 'static';
			option.elem = laydateContainer;
			var regExp = FOAMAT_REPLACE_REG[type],
			_value = elemValue || option.value,
			format = option.format;
			// 格式处理
			option.format = (format ? (regExp ? format.replace(regExp, '') : format) : FORMAT[type]).replace(/\s$/, '');
			// 初始化值
			if (_value && this.isInitValue(option, _value)) {
				option.value = _value;
				// 是否定位到初始值的tab页【默认定位】
				options.locationValueTab !== false && (selectIndex = i);
			}
			this.laydateObj.push(laydate.render(option));
			titleElem.append(laytitleContainer[0]);
			panlElem.append(laydateContainer);
			this.changeEvent(option, laytitleContainer);
		}
		options.valueToSelect

		tabElem.append(titleElem[0]);
		tabElem.append(panlElem[0]);
		lay('body').append(tabElem[0]);
		// 绑定事件，阻止事件冒泡
		tabElem.on('click', function(e) {
			e && e.stopPropagation ? e.stopPropagation() : (e.cancelBubble = true);
		});
		// 定位到初始值的tab页
		selectIndex !== null && this.changeTab(selectIndex);
		this.position();
	}

	// 校验是否需要初始化值
	Class.prototype.isInitValue = function(option, value){
		var that = this,
		formats = option.format.match(new RegExp(FORMAT_REG_EXP + '|.', 'g')) || [],
		reg = '',
		isInitValue = false;
		// 拼接正则表达式
		lay.each(formats, function(i, item){
			var len = item.length;
		    reg += ('(' + (/y|M|d|H|m|s/.test(item) ? '\\d{' + len +','+ (len > 2 ? 4 : 2) +'}' : item) + ')');
		});
		reg = new RegExp('^' + reg + '$');
		// 范围
		if(option.range){
			var rangeStr = ' ' + option.range + ' ',
			valueArr = value.split(rangeStr);
			value = valueArr[0];
			var endDate = valueArr[1];
			if (!reg.test(endDate)) {
				return false;
			}
		}
		isInitValue = reg.test(value);
		return isInitValue;
	}

	// 获取渲染的类型
	Class.prototype.getTypes = function(type) {
		if (this.types) {
			return this.types;
		}
		var typeArr = typeof type === 'string' ? TAB_FAST_TYPE[type] || [type] : type,
			exist = {},
			types = [];
		// 去重
		for (var i = 0, len = typeArr.length; i < len; i++) {
			!exist[typeArr[i]] && (exist[typeArr[i]] = true) && types.push(typeArr[i]);
		}
		return types;
	}

	// 完成之后函数
	Class.prototype.doneFn = function(fn) {
		var that = this;
		return function(value, date, endDate) {
			that.value = value;
			if (typeof fn === 'function') {
				that.value = fn(value, date, endDate) || value;
			}
			that.val(that.value);
			that.remove();
		}
	}
	Class.prototype.val = function(value){
		var valFn = this.isInput(this.elem[0]) ? 'val' : 'html';
		this.elem[valFn](value);
	}

	//是否输入框
	Class.prototype.isInput = function(elem){
	  return /input|textarea/.test(elem.tagName.toLocaleLowerCase());
	};

	// 标题栏切换事件
	Class.prototype.changeEvent = function(option, titleContainer) {
		var changeFn = option.changeTab,
		type = option.type,
		that = this;
		// 绑定切换事件
		titleContainer.on('click', function() {
			if (!lay(this).hasClass(SELECTED_CLASS)) {
				that.changeTab(this.index, changeFn, type);
			}
		});
	}
	Class.prototype.changeTab = function(index, changeFn, type){
		var titleElem = this.titleElem[0],
		panlElem = this.panlElem[0],
		panlChilds = panlElem.childNodes,
		titleChilds = titleElem.childNodes;
		for (var i = 0, len = titleChilds.length; i < len; i++) {
			var clsFn = i === index ? 'addClass' : 'removeClass'
			lay(titleChilds[i])[clsFn](SELECTED_CLASS);
			lay(panlChilds[i])[clsFn](SELECTED_CLASS);
		}
		typeof changeFn === 'function' && changeFn(type, titleChilds[index].innerHTML);
	}

	// 绑定事件
	Class.prototype.bindEvent = function() {
		var options = this.config,
			elem = this.elem,
			that = this;
		// 显示
		elem.on(options.trigger, function(elem, bind) {
			if (that.tabElem) {
				return;
			}
			that.render();
		});

		// 隐藏
		lay(document).on('click', function(e) {
			if (!that.tabElem || e.target === elem[0] || e.target === that.tabElem[0]) {
				return;
			}
			that.remove();
		});
		// 自适应
		lay(window).on('resize', function(e) {
			that.position();
		});
		lay(document).on('scroll', function(e) {
			that.position();
		});
	}

	//定位算法
	Class.prototype.position = function(){
	  // tabElem存在才可以定位
	  if (!this.tabElem) {
		  return ;
	  }
	  var that = this
	  ,options = that.config
	  ,elem = that.elem[0]
	  ,tabElem = that.tabElem[0]
	  ,rect = elem.getBoundingClientRect() //绑定元素的坐标
	  ,elemWidth = tabElem.offsetWidth //控件的宽度
	  ,elemHeight = tabElem.offsetHeight //控件的高度

	  ,winArea = function(type){
	    return document.documentElement[type ? 'clientWidth' : 'clientHeight']
	  }, margin = 5, left = rect.left, top = rect.bottom;

	  //如果右侧超出边界
	  if(left + elemWidth + margin > winArea('width')){
	    left = winArea('width') - elemWidth - margin;
	  }

	  //如果底部超出边界
	  if(top + elemHeight + margin > winArea()){
	    top = rect.top > elemHeight //顶部是否有足够区域显示完全
	      ? rect.top - elemHeight
	    : winArea() - elemHeight;
	    top = top;
	  }

	  tabElem.style.left = left + 'px';
	  tabElem.style.top = top + 'px';
	  // 成功渲染之后的回调
	  typeof options.success === 'function' && options.success(tabElem, options);
	};

	// 移除组件
	Class.prototype.remove = function(render) {
		var options = this.config;
		if (this.tabElem) {
			this.tabElem.remove();
			this.tabElem = null;
			// 成功渲染之后的回调
			typeof options.end === 'function' && options.end(this.elem[0]);
			
		}
	}

	// 渲染方法供外
	laydateTab.render = function(options) {
		var cls = new Class(options);
		return {
			config: cls.config
		};
	}

	// 加载css
	laydateTab.ready();
	//加载方式
	isLayui ? (
		layui.define('laydate', function(exports) { //layui加载
			lay = window.lay;
			laydate = layui.laydate;
			laydateTab.path = layui.cache.dir;
			exports(MOD_NAME, laydateTab);
		})
	) : (
		(typeof define === 'function' && define.amd) ? define(function() { //requirejs加载
			return laydateTab;
		}) : function() { //普通script标签加载
			window.laydateTab = laydateTab;
		}()
	);

}();
