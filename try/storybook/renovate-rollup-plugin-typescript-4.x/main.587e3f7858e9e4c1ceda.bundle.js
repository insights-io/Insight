(window.webpackJsonp=window.webpackJsonp||[]).push([[0],{195:function(module,__webpack_exports__,__webpack_require__){"use strict";__webpack_require__(42),__webpack_require__(48),__webpack_require__(49),__webpack_require__(46),__webpack_require__(172),__webpack_require__(50),__webpack_require__(91),__webpack_require__(252),__webpack_require__(65),__webpack_require__(43),__webpack_require__(173),__webpack_require__(66),__webpack_require__(118),__webpack_require__(371),__webpack_require__(104),__webpack_require__(794),__webpack_require__(795),__webpack_require__(80),__webpack_require__(36),__webpack_require__(92),__webpack_require__(796),__webpack_require__(68),__webpack_require__(51),__webpack_require__(52),__webpack_require__(53);var react__WEBPACK_IMPORTED_MODULE_25__=__webpack_require__(0),react__WEBPACK_IMPORTED_MODULE_25___default=__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_25__),baseui__WEBPACK_IMPORTED_MODULE_26__=__webpack_require__(6),baseui_block__WEBPACK_IMPORTED_MODULE_27__=__webpack_require__(15),baseui_input__WEBPACK_IMPORTED_MODULE_28__=__webpack_require__(154),baseui_form_control__WEBPACK_IMPORTED_MODULE_29__=__webpack_require__(127),baseui_button__WEBPACK_IMPORTED_MODULE_30__=__webpack_require__(898),baseui_phone_input__WEBPACK_IMPORTED_MODULE_31__=__webpack_require__(94),baseui_phone_input__WEBPACK_IMPORTED_MODULE_32__=__webpack_require__(896),baseui_phone_input__WEBPACK_IMPORTED_MODULE_33__=__webpack_require__(895),react_hook_form__WEBPACK_IMPORTED_MODULE_34__=__webpack_require__(272);function ownKeys(object,enumerableOnly){var keys=Object.keys(object);if(Object.getOwnPropertySymbols){var symbols=Object.getOwnPropertySymbols(object);enumerableOnly&&(symbols=symbols.filter((function(sym){return Object.getOwnPropertyDescriptor(object,sym).enumerable}))),keys.push.apply(keys,symbols)}return keys}function _objectSpread(target){for(var source,i=1;i<arguments.length;i++)source=null!=arguments[i]?arguments[i]:{},i%2?ownKeys(Object(source),!0).forEach((function(key){_defineProperty(target,key,source[key])})):Object.getOwnPropertyDescriptors?Object.defineProperties(target,Object.getOwnPropertyDescriptors(source)):ownKeys(Object(source)).forEach((function(key){Object.defineProperty(target,key,Object.getOwnPropertyDescriptor(source,key))}));return target}function _defineProperty(obj,key,value){return key in obj?Object.defineProperty(obj,key,{value:value,enumerable:!0,configurable:!0,writable:!0}):obj[key]=value,obj}function _objectWithoutProperties(source,excluded){if(null==source)return{};var key,i,target=function _objectWithoutPropertiesLoose(source,excluded){if(null==source)return{};var key,i,target={},sourceKeys=Object.keys(source);for(i=0;i<sourceKeys.length;i++)key=sourceKeys[i],0<=excluded.indexOf(key)||(target[key]=source[key]);return target}(source,excluded);if(Object.getOwnPropertySymbols){var sourceSymbolKeys=Object.getOwnPropertySymbols(source);for(i=0;i<sourceSymbolKeys.length;i++)key=sourceSymbolKeys[i],0<=excluded.indexOf(key)||Object.prototype.propertyIsEnumerable.call(source,key)&&(target[key]=source[key])}return target}function _slicedToArray(arr,i){return function _arrayWithHoles(arr){if(Array.isArray(arr))return arr}(arr)||function _iterableToArrayLimit(arr,i){if("undefined"==typeof Symbol||!(Symbol.iterator in Object(arr)))return;var _arr=[],_n=!0,_d=!1,_e=void 0;try{for(var _s,_i=arr[Symbol.iterator]();!(_n=(_s=_i.next()).done)&&(_arr.push(_s.value),!i||_arr.length!==i);_n=!0);}catch(err){_d=!0,_e=err}finally{try{_n||null==_i.return||_i.return()}finally{if(_d)throw _e}}return _arr}(arr,i)||function _unsupportedIterableToArray(o,minLen){if(!o)return;if("string"==typeof o)return _arrayLikeToArray(o,minLen);var n=Object.prototype.toString.call(o).slice(8,-1);"Object"===n&&o.constructor&&(n=o.constructor.name);if("Map"===n||"Set"===n)return Array.from(o);if("Arguments"===n||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n))return _arrayLikeToArray(o,minLen)}(arr,i)||function _nonIterableRest(){throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.")}()}function _arrayLikeToArray(arr,len){(null==len||len>arr.length)&&(len=arr.length);for(var i=0,arr2=Array(len);i<len;i++)arr2[i]=arr[i];return arr2}var SignUpForm=function(_ref){var _errors$firstName,_errors$lastName,_errors$company,_errors$phoneNumber,_errors$email,_errors$password,onSubmitProp=_ref.onSubmit,_ref$minPasswordLengt=_ref.minPasswordLength,minPasswordLength=void 0===_ref$minPasswordLengt?8:_ref$minPasswordLengt,_useState2=_slicedToArray(Object(react__WEBPACK_IMPORTED_MODULE_25__.useState)(!1),2),isSubmitting=_useState2[0],setIsSubmitting=_useState2[1],_useState4=_slicedToArray(Object(react__WEBPACK_IMPORTED_MODULE_25__.useState)(baseui_phone_input__WEBPACK_IMPORTED_MODULE_31__.a.US),2),country=_useState4[0],setCountry=_useState4[1],_useStyletron2=_slicedToArray(Object(baseui__WEBPACK_IMPORTED_MODULE_26__.b)(),2),css=_useStyletron2[0],theme=_useStyletron2[1],_useForm=Object(react_hook_form__WEBPACK_IMPORTED_MODULE_34__.b)(),register=_useForm.register,handleSubmit=_useForm.handleSubmit,errors=_useForm.errors,control=_useForm.control,onSubmit=handleSubmit((function(_ref2){var phoneNumber=_ref2.phoneNumber,rest=_objectWithoutProperties(_ref2,["phoneNumber"]);if(!isSubmitting){setIsSubmitting(!0);var formData=phoneNumber?_objectSpread(_objectSpread({},rest),{},{phoneNumber:"".concat(country.dialCode).concat(phoneNumber)}):rest;onSubmitProp(formData).finally((function(){setIsSubmitting(!1)}))}})),inputBorderRadius={borderBottomRightRadius:theme.sizing.scale100,borderTopRightRadius:theme.sizing.scale100,borderTopLeftRadius:theme.sizing.scale100,borderBottomLeftRadius:theme.sizing.scale100},inputOverrides={InputContainer:{style:inputBorderRadius},Input:{style:inputBorderRadius}};return react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement("form",{onSubmit:onSubmit,noValidate:!0},react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_block__WEBPACK_IMPORTED_MODULE_27__.a,{display:"flex"},react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_block__WEBPACK_IMPORTED_MODULE_27__.a,{width:"100%",marginRight:theme.sizing.scale600},react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_form_control__WEBPACK_IMPORTED_MODULE_29__.a,{label:"First name",error:null===(_errors$firstName=errors.firstName)||void 0===_errors$firstName?void 0:_errors$firstName.message},react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_input__WEBPACK_IMPORTED_MODULE_28__.a,{overrides:inputOverrides,name:"firstName",placeholder:"First name",required:!0,inputRef:register({required:"Required"}),error:!!errors.firstName}))),react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_block__WEBPACK_IMPORTED_MODULE_27__.a,{width:"100%",marginLeft:theme.sizing.scale600},react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_form_control__WEBPACK_IMPORTED_MODULE_29__.a,{label:"Last Name",error:null===(_errors$lastName=errors.lastName)||void 0===_errors$lastName?void 0:_errors$lastName.message},react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_input__WEBPACK_IMPORTED_MODULE_28__.a,{overrides:inputOverrides,name:"lastName",placeholder:"Last name",required:!0,inputRef:register({required:"Required"}),error:!!errors.lastName})))),react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_block__WEBPACK_IMPORTED_MODULE_27__.a,null,react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_form_control__WEBPACK_IMPORTED_MODULE_29__.a,{label:"Company",error:null===(_errors$company=errors.company)||void 0===_errors$company?void 0:_errors$company.message},react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_input__WEBPACK_IMPORTED_MODULE_28__.a,{overrides:inputOverrides,placeholder:"Company",name:"company",inputRef:register({required:"Required"}),error:!!errors.company}))),react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_block__WEBPACK_IMPORTED_MODULE_27__.a,null,react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_form_control__WEBPACK_IMPORTED_MODULE_29__.a,{error:null===(_errors$phoneNumber=errors.phoneNumber)||void 0===_errors$phoneNumber?void 0:_errors$phoneNumber.message,label:function label(){return react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement("div",null,"Phone"," ",react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement("span",{className:css({color:theme.colors.primary400})},"(optional)"))}},react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(react_hook_form__WEBPACK_IMPORTED_MODULE_34__.a,{control:control,name:"phoneNumber",defaultValue:"",rules:{pattern:{value:/^(?![+]).*$/,message:"Please enter a phone number without the country dial code."}},as:react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_phone_input__WEBPACK_IMPORTED_MODULE_32__.a,{placeholder:"Phone number",country:country,onCountryChange:function onCountryChange(_ref3){var option=_ref3.option;return setCountry(option)},error:!!errors.phoneNumber,overrides:{CountrySelect:{props:{overrides:{Dropdown:{component:baseui_phone_input__WEBPACK_IMPORTED_MODULE_33__.a}}}}}})}))),react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_block__WEBPACK_IMPORTED_MODULE_27__.a,null,react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_form_control__WEBPACK_IMPORTED_MODULE_29__.a,{label:"Email",error:null===(_errors$email=errors.email)||void 0===_errors$email?void 0:_errors$email.message},react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_input__WEBPACK_IMPORTED_MODULE_28__.a,{overrides:inputOverrides,name:"email",type:"email",placeholder:"Email",required:!0,inputRef:register({required:"Required",pattern:{value:/^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i,message:"Invalid email address"}}),error:!!errors.email}))),react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_block__WEBPACK_IMPORTED_MODULE_27__.a,{marginBottom:theme.sizing.scale1200},react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_form_control__WEBPACK_IMPORTED_MODULE_29__.a,{label:"Password",error:null===(_errors$password=errors.password)||void 0===_errors$password?void 0:_errors$password.message},react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_input__WEBPACK_IMPORTED_MODULE_28__.a,{overrides:inputOverrides,placeholder:"Password",name:"password",type:"password",ref:register,inputRef:register({required:"Required",minLength:{value:minPasswordLength,message:"Password must be at least ".concat(minPasswordLength," characters long")}}),error:!!errors.password}))),react__WEBPACK_IMPORTED_MODULE_25___default.a.createElement(baseui_button__WEBPACK_IMPORTED_MODULE_30__.a,{type:"submit",$style:{width:"100%"},isLoading:isSubmitting},"Get started"))};SignUpForm.displayName="SignUpForm",SignUpForm.__docgenInfo={description:"",methods:[],displayName:"SignUpForm",props:{minPasswordLength:{defaultValue:{value:"8",computed:!1},required:!1,tsType:{name:"number"},description:""},onSubmit:{required:!0,tsType:{name:"signature",type:"function",raw:"<T>(data: FormData) => Promise<T>",signature:{arguments:[{name:"data",type:{name:"signature",type:"object",raw:"{\n  firstName: string;\n  lastName: string;\n  company: string;\n  phoneNumber?: string;\n  email: string;\n  password: string;\n}",signature:{properties:[{key:"firstName",value:{name:"string",required:!0}},{key:"lastName",value:{name:"string",required:!0}},{key:"company",value:{name:"string",required:!0}},{key:"phoneNumber",value:{name:"string",required:!1}},{key:"email",value:{name:"string",required:!0}},{key:"password",value:{name:"string",required:!0}}]}}}],return:{name:"Promise",elements:[{name:"T"}],raw:"Promise<T>"}}},description:""}}},__webpack_exports__.a=SignUpForm,"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/components/SignUpForm/SignUpForm.tsx"]={name:"SignUpForm",docgenInfo:SignUpForm.__docgenInfo,path:"src/components/SignUpForm/SignUpForm.tsx"})},428:function(module,exports,__webpack_require__){__webpack_require__(429),__webpack_require__(574),__webpack_require__(892),module.exports=__webpack_require__(787)},493:function(module,exports){},787:function(module,__webpack_exports__,__webpack_require__){"use strict";__webpack_require__.r(__webpack_exports__),function(module){var _storybook_react__WEBPACK_IMPORTED_MODULE_0__=__webpack_require__(196);module._StorybookPreserveDecorators=!0,Object(_storybook_react__WEBPACK_IMPORTED_MODULE_0__.configure)([__webpack_require__(789)],module)}.call(this,__webpack_require__(788)(module))},789:function(module,exports,__webpack_require__){var map={"./components/GetStarted/GetStarted.stories.tsx":893,"./components/SignUpForm/SignUpForm.stories.tsx":891};function webpackContext(req){var id=webpackContextResolve(req);return __webpack_require__(id)}function webpackContextResolve(req){if(!__webpack_require__.o(map,req)){var e=new Error("Cannot find module '"+req+"'");throw e.code="MODULE_NOT_FOUND",e}return map[req]}webpackContext.keys=function webpackContextKeys(){return Object.keys(map)},webpackContext.resolve=webpackContextResolve,module.exports=webpackContext,webpackContext.id=789},891:function(module,__webpack_exports__,__webpack_require__){"use strict";__webpack_require__.r(__webpack_exports__),__webpack_require__.d(__webpack_exports__,"Base",(function(){return Base}));__webpack_require__(67),__webpack_require__(36),__webpack_require__(92),__webpack_require__(183);var react__WEBPACK_IMPORTED_MODULE_4__=__webpack_require__(0),react__WEBPACK_IMPORTED_MODULE_4___default=__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_4__),_storybook_addon_actions__WEBPACK_IMPORTED_MODULE_5__=__webpack_require__(45),baseui_block__WEBPACK_IMPORTED_MODULE_6__=__webpack_require__(15),_SignUpForm__WEBPACK_IMPORTED_MODULE_7__=__webpack_require__(195);function _extends(){return(_extends=Object.assign||function(target){for(var source,i=1;i<arguments.length;i++)for(var key in source=arguments[i])Object.prototype.hasOwnProperty.call(source,key)&&(target[key]=source[key]);return target}).apply(this,arguments)}__webpack_exports__.default={title:"SignUpForm"};var baseProps={onSubmit:function onSubmit(data){return new Promise((function(resolve){setTimeout((function(){return resolve(Object(_storybook_addon_actions__WEBPACK_IMPORTED_MODULE_5__.action)("onSubmit")(data))}),200)}))}},Base=function(storyProps){return react__WEBPACK_IMPORTED_MODULE_4___default.a.createElement(baseui_block__WEBPACK_IMPORTED_MODULE_6__.a,{width:"100%",maxWidth:"720px",marginLeft:"auto",marginRight:"auto"},react__WEBPACK_IMPORTED_MODULE_4___default.a.createElement(_SignUpForm__WEBPACK_IMPORTED_MODULE_7__.a,_extends({},baseProps,storyProps)))};Base.displayName="Base",Base.__docgenInfo={description:"",methods:[],displayName:"Base"},"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/components/SignUpForm/SignUpForm.stories.tsx"]={name:"Base",docgenInfo:Base.__docgenInfo,path:"src/components/SignUpForm/SignUpForm.stories.tsx"})},892:function(module,__webpack_exports__,__webpack_require__){"use strict";__webpack_require__.r(__webpack_exports__);var client=__webpack_require__(196),react=(__webpack_require__(36),__webpack_require__(92),__webpack_require__(0)),react_default=__webpack_require__.n(react),public_api=__webpack_require__(125),dist=__webpack_require__(45),router_context=__webpack_require__(271),next_router=__webpack_require__(415),browser_es5_es=__webpack_require__(151),dist_browser_es5_es=__webpack_require__(97),styletron="undefined"==typeof window?new browser_es5_es.b:new browser_es5_es.a({hydrate:document.getElementsByClassName("_styletron_hydrate_")}),base_provider=__webpack_require__(906),light_theme=__webpack_require__(897),ThemeProvider=function(_ref){var children=_ref.children;return react_default.a.createElement(base_provider.a,{theme:light_theme.a,overrides:{AppContainer:{style:{height:"100%",display:"flex",flexDirection:"column"}}}},children)};ThemeProvider.displayName="ThemeProvider",ThemeProvider.__docgenInfo={description:"",methods:[],displayName:"ThemeProvider",props:{children:{required:!0,tsType:{name:"JSX.Element"},description:""}}};var ThemeProvider_ThemeProvider=ThemeProvider;"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/shared/containers/ThemeProvider/ThemeProvider.tsx"]={name:"ThemeProvider",docgenInfo:ThemeProvider.__docgenInfo,path:"src/shared/containers/ThemeProvider/ThemeProvider.tsx"});var AppProviders=function(_ref){var children=_ref.children,_ref$engine=_ref.engine,engine=void 0===_ref$engine?styletron:_ref$engine;return react_default.a.createElement(dist_browser_es5_es.a,{value:engine,debug:void 0,debugAfterHydration:!0},react_default.a.createElement(ThemeProvider_ThemeProvider,null,children))};AppProviders.displayName="AppProviders",AppProviders.__docgenInfo={description:"",methods:[],displayName:"AppProviders",props:{engine:{defaultValue:{value:"styletron",computed:!0},required:!1,tsType:{name:"union",raw:"Client | Server",elements:[{name:"Client"},{name:"Server"}]},description:""},children:{required:!0,tsType:{name:"JSX.Element"},description:""}}};var AppProviders_AppProviders=AppProviders;"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/shared/containers/AppProviders/AppProviders.tsx"]={name:"AppProviders",docgenInfo:AppProviders.__docgenInfo,path:"src/shared/containers/AppProviders/AppProviders.tsx"});var nextDecorator_engine=new browser_es5_es.a,_ref2=react_default.a.createElement("div",null,"Hello world"),nextDecorator=Object(public_api.makeDecorator)({name:"nextDecorator",parameterName:"next__decorator",skipIfNoParametersOrOptions:!1,wrapper:function wrapper(story,context,_ref){_ref.parameters;var router={route:"/",pathname:"/",query:{},asPath:"/",push:function push(){for(var _len=arguments.length,args=Array(_len),_key=0;_key<_len;_key++)args[_key]=arguments[_key];return Object(dist.action)("push")(args),Promise.resolve(!0)},back:Object(dist.action)("back"),replace:function replace(){for(var _len2=arguments.length,args=Array(_len2),_key2=0;_key2<_len2;_key2++)args[_key2]=arguments[_key2];return Object(dist.action)("replace")(args),Promise.resolve(!0)},reload:Object(dist.action)("reload"),beforePopState:Object(dist.action)("beforePopState"),prefetch:function prefetch(){for(var _len3=arguments.length,args=Array(_len3),_key3=0;_key3<_len3;_key3++)args[_key3]=arguments[_key3];return Object(dist.action)("prefetch")(args),Promise.resolve()},events:{on:Object(dist.action)("events.on"),off:Object(dist.action)("events.off"),emit:Object(dist.action)("events.emit")},isFallback:!1};return Object(next_router.createRouter)(router.pathname,router.query,router.asPath,{isFallback:router.isFallback,pageLoader:{loadPage:function loadPage(){return Object(dist.action)("loadPage").apply(void 0,arguments),Promise.resolve((function(){return _ref2}))}}}),react_default.a.createElement(AppProviders_AppProviders,{engine:nextDecorator_engine},react_default.a.createElement(router_context.RouterContext.Provider,{value:router},story(context)))}});Object(client.addDecorator)(nextDecorator)},893:function(module,__webpack_exports__,__webpack_require__){"use strict";__webpack_require__.r(__webpack_exports__),__webpack_require__.d(__webpack_exports__,"Base",(function(){return Base}));var react=__webpack_require__(0),react_default=__webpack_require__.n(react),head=(__webpack_require__(42),__webpack_require__(48),__webpack_require__(49),__webpack_require__(91),__webpack_require__(65),__webpack_require__(43),__webpack_require__(173),__webpack_require__(66),__webpack_require__(118),__webpack_require__(36),__webpack_require__(92),__webpack_require__(68),__webpack_require__(51),__webpack_require__(53),__webpack_require__(183),__webpack_require__(417)),head_default=__webpack_require__.n(head),styled=__webpack_require__(6),typography=__webpack_require__(202),block=__webpack_require__(15),button_button=__webpack_require__(898),constants=__webpack_require__(10),SignUpForm=__webpack_require__(195),shared_config={appBaseURL:Object({NODE_ENV:"production",NODE_PATH:"",PUBLIC_URL:"."}).APP_BASE_URL||"http://localhost:3000",helpBaseURL:Object({NODE_ENV:"production",NODE_PATH:"",PUBLIC_URL:"."}).HELP_BASE_URL||"http://localhost:3003"};function _slicedToArray(arr,i){return function _arrayWithHoles(arr){if(Array.isArray(arr))return arr}(arr)||function _iterableToArrayLimit(arr,i){if("undefined"==typeof Symbol||!(Symbol.iterator in Object(arr)))return;var _arr=[],_n=!0,_d=!1,_e=void 0;try{for(var _s,_i=arr[Symbol.iterator]();!(_n=(_s=_i.next()).done)&&(_arr.push(_s.value),!i||_arr.length!==i);_n=!0);}catch(err){_d=!0,_e=err}finally{try{_n||null==_i.return||_i.return()}finally{if(_d)throw _e}}return _arr}(arr,i)||function _unsupportedIterableToArray(o,minLen){if(!o)return;if("string"==typeof o)return _arrayLikeToArray(o,minLen);var n=Object.prototype.toString.call(o).slice(8,-1);"Object"===n&&o.constructor&&(n=o.constructor.name);if("Map"===n||"Set"===n)return Array.from(o);if("Arguments"===n||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n))return _arrayLikeToArray(o,minLen)}(arr,i)||function _nonIterableRest(){throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.")}()}function _arrayLikeToArray(arr,len){(null==len||len>arr.length)&&(len=arr.length);for(var i=0,arr2=Array(len);i<len;i++)arr2[i]=arr[i];return arr2}var _ref=react_default.a.createElement(head_default.a,null,react_default.a.createElement("title",null,"Insight | Sign up")),_ref2=react_default.a.createElement(typography.b,{margin:0},"Insight"),GetStarted=function(){var _useStyletron2=_slicedToArray(Object(styled.b)(),2),css=_useStyletron2[0],theme=_useStyletron2[1];return react_default.a.createElement(block.a,{display:"flex",flexDirection:"column",height:"100%"},_ref,react_default.a.createElement("nav",{className:css({padding:theme.sizing.scale600,borderBottom:"1px solid ".concat(theme.colors.primary200)})},react_default.a.createElement(block.a,{display:"flex",justifyContent:"space-between"},_ref2,react_default.a.createElement(block.a,null,react_default.a.createElement("a",{onClick:function onClick(event){event.preventDefault(),event.stopPropagation()},href:shared_config.helpBaseURL,className:css({marginRight:theme.sizing.scale600,textDecoration:"none"})},react_default.a.createElement(button_button.a,{shape:constants.b.pill,size:"compact",kind:"minimal"},"Help")),react_default.a.createElement("a",{href:shared_config.appBaseURL,className:css({textDecoration:"none"})},react_default.a.createElement(button_button.a,{shape:constants.b.pill,size:"compact",kind:"minimal"},"Log in"))))),react_default.a.createElement(block.a,{height:"100%",padding:theme.sizing.scale600},react_default.a.createElement(block.a,{width:"100%",maxWidth:"720px",marginLeft:"auto",marginRight:"auto"},react_default.a.createElement(block.a,{marginBottom:theme.sizing.scale700,$style:{textAlign:"center"}},react_default.a.createElement(typography.a,{marginBottom:theme.sizing.scale400,$style:{fontWeight:700}},"Start your free trial now."),react_default.a.createElement(typography.c,{marginTop:theme.sizing.scale400,color:theme.colors.primary400},"You're minutes away from insights.")),react_default.a.createElement(SignUpForm.a,{onSubmit:function onSubmit(){return new Promise((function(resolve){return setTimeout(resolve,200)}))}}))))};GetStarted.displayName="GetStarted",GetStarted.__docgenInfo={description:"",methods:[],displayName:"GetStarted"};var GetStarted_GetStarted=GetStarted;"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/components/GetStarted/GetStarted.tsx"]={name:"GetStarted",docgenInfo:GetStarted.__docgenInfo,path:"src/components/GetStarted/GetStarted.tsx"});__webpack_exports__.default={title:"GetStarted"};var GetStarted_stories_ref=react_default.a.createElement(GetStarted_GetStarted,null),Base=function(){return GetStarted_stories_ref};Base.displayName="Base",Base.__docgenInfo={description:"",methods:[],displayName:"Base"},"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/components/GetStarted/GetStarted.stories.tsx"]={name:"Base",docgenInfo:Base.__docgenInfo,path:"src/components/GetStarted/GetStarted.stories.tsx"})}},[[428,1,2]]]);
//# sourceMappingURL=main.587e3f7858e9e4c1ceda.bundle.js.map