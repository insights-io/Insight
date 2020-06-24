(window.webpackJsonp=window.webpackJsonp||[]).push([[0],{150:function(module,exports,__webpack_require__){"use strict";module.exports=__webpack_require__(739)},375:function(module,exports,__webpack_require__){__webpack_require__(376),__webpack_require__(522),__webpack_require__(796),module.exports=__webpack_require__(729)},440:function(module,exports){},729:function(module,__webpack_exports__,__webpack_require__){"use strict";__webpack_require__.r(__webpack_exports__),function(module){var _storybook_react__WEBPACK_IMPORTED_MODULE_0__=__webpack_require__(145);module._StorybookPreserveDecorators=!0,Object(_storybook_react__WEBPACK_IMPORTED_MODULE_0__.configure)([__webpack_require__(731)],module)}.call(this,__webpack_require__(730)(module))},731:function(module,exports,__webpack_require__){var map={"./modules/app/components/GlobalSearch/GlobalSearch.stories.tsx":797,"./modules/auth/components/Login/Login.stories.tsx":795};function webpackContext(req){var id=webpackContextResolve(req);return __webpack_require__(id)}function webpackContextResolve(req){if(!__webpack_require__.o(map,req)){var e=new Error("Cannot find module '"+req+"'");throw e.code="MODULE_NOT_FOUND",e}return map[req]}webpackContext.keys=function webpackContextKeys(){return Object.keys(map)},webpackContext.resolve=webpackContextResolve,module.exports=webpackContext,webpackContext.id=731},739:function(module,exports,__webpack_require__){"use strict";function r(r){return r&&"object"==typeof r&&"default"in r?r.default:r}Object.defineProperty(exports,"__esModule",{value:!0});var e=__webpack_require__(0),t=r(e),n=r(__webpack_require__(740));function o(r){return function(t,o){return function(r){var t=n.createSandbox(),o=e.useRef(!1);o.current||r(t),o.current=!0,e.useEffect((function(){return function(){t.restore()}}),[t])}(r),t(o)}}function c(){return(c=Object.assign||function(r){for(var e=1;e<arguments.length;e++){var t=arguments[e];for(var n in t)Object.prototype.hasOwnProperty.call(t,n)&&(r[n]=t[n])}return r}).apply(this,arguments)}exports.configureStory=function(r){var e=r.setupMocks,t=r.decorators,n=void 0===t?[]:t;return c({},function(r,e){if(null==r)return{};var t,n,o={},c=Object.keys(r);for(n=0;n<c.length;n++)e.indexOf(t=c[n])>=0||(o[t]=r[t]);return o}(r,["setupMocks","decorators"]),{decorators:e?[].concat(n,[o(e)]):n,setupMocks:e})},exports.fullHeightDecorator=function(r,e){return t.createElement(t.Fragment,null,t.createElement("style",{dangerouslySetInnerHTML:{__html:"\n            html, body, #root, #root > div:first-child { height: 100%; }\n            .sb-show-main { margin: 0px; }\n            #root > div:first-child {\n              display: flex;\n              flex-direction: column;\n            }\n        "}}),r(e))},exports.mockApiError=function(r){var e=new Error("APIError");return Object.assign(e,{response:{json:function(){return{error:r}}}}),e},exports.mockDecorator=o},795:function(module,__webpack_exports__,__webpack_require__){"use strict";__webpack_require__.r(__webpack_exports__),__webpack_require__.d(__webpack_exports__,"Base",(function(){return Base})),__webpack_require__.d(__webpack_exports__,"InvalidPassword",(function(){return InvalidPassword}));__webpack_require__(11),__webpack_require__(74),__webpack_require__(203);var react=__webpack_require__(0),react_default=__webpack_require__.n(react),dist=__webpack_require__(150),browser=(__webpack_require__(15),__webpack_require__(20),__webpack_require__(22),__webpack_require__(785),__webpack_require__(114)),authApiBaseURL=Object({NODE_ENV:"production",NODE_PATH:"",PUBLIC_URL:"."}).NEXT_PUBLIC_AUTH_API_BASE_URL||"http://localhost:8080",sso=(Object({NODE_ENV:"production",NODE_PATH:"",PUBLIC_URL:"."}).NEXT_PUBLIC_SESSION_API_BASE_URL,{login:function login(email,password){var baseURL=2<arguments.length&&void 0!==arguments[2]?arguments[2]:authApiBaseURL,body=new URLSearchParams;return body.set("email",email),body.set("password",password),browser.a.post("".concat(baseURL,"/v1/sso/login"),{body:body,credentials:"include"}).json()},session:function session(sessionId){var baseURL=1<arguments.length&&void 0!==arguments[1]?arguments[1]:authApiBaseURL;return browser.a.get("".concat(baseURL,"/v1/sso/session"),{searchParams:{id:sessionId}})},me:function me(){var baseURL=0<arguments.length&&void 0!==arguments[0]?arguments[0]:authApiBaseURL;return browser.a.get("".concat(baseURL,"/v1/sso/me"),{credentials:"include"})},logout:function logout(){var baseURL=0<arguments.length&&void 0!==arguments[0]?arguments[0]:authApiBaseURL;return browser.a.post("".concat(baseURL,"/v1/sso/logout"),{credentials:"include"})}}),head=(__webpack_require__(21),__webpack_require__(24),__webpack_require__(25),__webpack_require__(33),__webpack_require__(34),__webpack_require__(28),__webpack_require__(65),__webpack_require__(29),__webpack_require__(47),__webpack_require__(789),__webpack_require__(64),__webpack_require__(30),__webpack_require__(317),__webpack_require__(116),__webpack_require__(368)),head_default=__webpack_require__.n(head),form_control=__webpack_require__(815),input=__webpack_require__(812),styled=__webpack_require__(226),block=__webpack_require__(16),react_hook_form_es=__webpack_require__(369),button_button=__webpack_require__(813),next_router=__webpack_require__(146);__webpack_require__(55);function _extends(){return(_extends=Object.assign||function(target){for(var source,i=1;i<arguments.length;i++)for(var key in source=arguments[i])Object.prototype.hasOwnProperty.call(source,key)&&(target[key]=source[key]);return target}).apply(this,arguments)}function _slicedToArray(arr,i){return function _arrayWithHoles(arr){if(Array.isArray(arr))return arr}(arr)||function _iterableToArrayLimit(arr,i){if("undefined"==typeof Symbol||!(Symbol.iterator in Object(arr)))return;var _arr=[],_n=!0,_d=!1,_e=void 0;try{for(var _s,_i=arr[Symbol.iterator]();!(_n=(_s=_i.next()).done)&&(_arr.push(_s.value),!i||_arr.length!==i);_n=!0);}catch(err){_d=!0,_e=err}finally{try{_n||null==_i.return||_i.return()}finally{if(_d)throw _e}}return _arr}(arr,i)||function _unsupportedIterableToArray(o,minLen){if(!o)return;if("string"==typeof o)return _arrayLikeToArray(o,minLen);var n=Object.prototype.toString.call(o).slice(8,-1);"Object"===n&&o.constructor&&(n=o.constructor.name);if("Map"===n||"Set"===n)return Array.from(o);if("Arguments"===n||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n))return _arrayLikeToArray(o,minLen)}(arr,i)||function _nonIterableRest(){throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.")}()}function _arrayLikeToArray(arr,len){(null==len||len>arr.length)&&(len=arr.length);for(var i=0,arr2=Array(len);i<len;i++)arr2[i]=arr[i];return arr2}var Divider=function(props){var _useStyletron2=_slicedToArray(Object(styled.b)(),2),theme=(_useStyletron2[0],_useStyletron2[1]);return react_default.a.createElement(block.a,_extends({height:"1px",marginTop:theme.sizing.scale800,marginBottom:theme.sizing.scale800,backgroundColor:theme.colors.primary100},props))};Divider.displayName="Divider",Divider.__docgenInfo={description:"",methods:[],displayName:"Divider"};var Divider_Divider=Divider;"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/shared/components/Divider/Divider.tsx"]={name:"Divider",docgenInfo:Divider.__docgenInfo,path:"src/shared/components/Divider/Divider.tsx"});var next_link=__webpack_require__(370),link_default=__webpack_require__.n(next_link),typography=__webpack_require__(110);function FormError_slicedToArray(arr,i){return function FormError_arrayWithHoles(arr){if(Array.isArray(arr))return arr}(arr)||function FormError_iterableToArrayLimit(arr,i){if("undefined"==typeof Symbol||!(Symbol.iterator in Object(arr)))return;var _arr=[],_n=!0,_d=!1,_e=void 0;try{for(var _s,_i=arr[Symbol.iterator]();!(_n=(_s=_i.next()).done)&&(_arr.push(_s.value),!i||_arr.length!==i);_n=!0);}catch(err){_d=!0,_e=err}finally{try{_n||null==_i.return||_i.return()}finally{if(_d)throw _e}}return _arr}(arr,i)||function FormError_unsupportedIterableToArray(o,minLen){if(!o)return;if("string"==typeof o)return FormError_arrayLikeToArray(o,minLen);var n=Object.prototype.toString.call(o).slice(8,-1);"Object"===n&&o.constructor&&(n=o.constructor.name);if("Map"===n||"Set"===n)return Array.from(o);if("Arguments"===n||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n))return FormError_arrayLikeToArray(o,minLen)}(arr,i)||function FormError_nonIterableRest(){throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.")}()}function FormError_arrayLikeToArray(arr,len){(null==len||len>arr.length)&&(len=arr.length);for(var i=0,arr2=Array(len);i<len;i++)arr2[i]=arr[i];return arr2}var FormError=function(_ref){var error=_ref.error,_useStyletron2=FormError_slicedToArray(Object(styled.b)(),2),theme=(_useStyletron2[0],_useStyletron2[1]);return react_default.a.createElement(block.a,{display:"flex",justifyContent:"center",marginTop:theme.sizing.scale600},react_default.a.createElement(typography.c,{color:theme.colors.borderError},error.message))};FormError.displayName="FormError",FormError.__docgenInfo={description:"",methods:[],displayName:"FormError",props:{error:{required:!0,tsType:{name:"APIError"},description:""}}};var FormError_FormError=react_default.a.memo(FormError);"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/shared/components/FormError/FormError.tsx"]={name:"FormError",docgenInfo:FormError.__docgenInfo,path:"src/shared/components/FormError/FormError.tsx"});var EMAIL_VALIDATION={required:"Required",pattern:{value:/^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i,message:"Please enter a valid email address"}},PASSWORD_VALIDATION={required:"Required",minLength:{value:8,message:"Password must be at least ".concat(8," characters long")}},TRY_BASE_URL=Object({NODE_ENV:"production",NODE_PATH:"",PUBLIC_URL:"."}).NEXT_PUBLIC_TRY_BASE_URL||"http://localhost:3002";function AuthPageLayout_slicedToArray(arr,i){return function AuthPageLayout_arrayWithHoles(arr){if(Array.isArray(arr))return arr}(arr)||function AuthPageLayout_iterableToArrayLimit(arr,i){if("undefined"==typeof Symbol||!(Symbol.iterator in Object(arr)))return;var _arr=[],_n=!0,_d=!1,_e=void 0;try{for(var _s,_i=arr[Symbol.iterator]();!(_n=(_s=_i.next()).done)&&(_arr.push(_s.value),!i||_arr.length!==i);_n=!0);}catch(err){_d=!0,_e=err}finally{try{_n||null==_i.return||_i.return()}finally{if(_d)throw _e}}return _arr}(arr,i)||function AuthPageLayout_unsupportedIterableToArray(o,minLen){if(!o)return;if("string"==typeof o)return AuthPageLayout_arrayLikeToArray(o,minLen);var n=Object.prototype.toString.call(o).slice(8,-1);"Object"===n&&o.constructor&&(n=o.constructor.name);if("Map"===n||"Set"===n)return Array.from(o);if("Arguments"===n||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n))return AuthPageLayout_arrayLikeToArray(o,minLen)}(arr,i)||function AuthPageLayout_nonIterableRest(){throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.")}()}function AuthPageLayout_arrayLikeToArray(arr,len){(null==len||len>arr.length)&&(len=arr.length);for(var i=0,arr2=Array(len);i<len;i++)arr2[i]=arr[i];return arr2}var AuthPageLayout=function(_ref){var children=_ref.children,subtitle=_ref.subtitle,_useStyletron2=AuthPageLayout_slicedToArray(Object(styled.b)(),2),theme=(_useStyletron2[0],_useStyletron2[1]);return react_default.a.createElement(block.a,{display:"flex",flexDirection:"column",height:"100%"},react_default.a.createElement(block.a,{height:"100%",width:"100%",maxWidth:"720px",marginLeft:"auto",marginRight:"auto",display:"flex",flexDirection:"column",justifyContent:"center",padding:theme.sizing.scale600},react_default.a.createElement(block.a,{marginBottom:theme.sizing.scale700,$style:{textAlign:"center"}},react_default.a.createElement(typography.a,{marginBottom:theme.sizing.scale400,$style:{fontWeight:700,fontSize:"24px"}},"Insight"),subtitle&&react_default.a.createElement(typography.b,{marginTop:theme.sizing.scale400,$style:{fontWeight:700,fontSize:"18px"}},subtitle)),children))};AuthPageLayout.displayName="AuthPageLayout",AuthPageLayout.__docgenInfo={description:"",methods:[],displayName:"AuthPageLayout",props:{children:{required:!0,tsType:{name:"ReactReactNode",raw:"React.ReactNode"},description:""},subtitle:{required:!1,tsType:{name:"string"},description:""}}};var PageLayout_AuthPageLayout=AuthPageLayout;function asyncGeneratorStep(gen,resolve,reject,_next,_throw,key,arg){try{var info=gen[key](arg),value=info.value}catch(error){return void reject(error)}info.done?resolve(value):Promise.resolve(value).then(_next,_throw)}function Login_slicedToArray(arr,i){return function Login_arrayWithHoles(arr){if(Array.isArray(arr))return arr}(arr)||function Login_iterableToArrayLimit(arr,i){if("undefined"==typeof Symbol||!(Symbol.iterator in Object(arr)))return;var _arr=[],_n=!0,_d=!1,_e=void 0;try{for(var _s,_i=arr[Symbol.iterator]();!(_n=(_s=_i.next()).done)&&(_arr.push(_s.value),!i||_arr.length!==i);_n=!0);}catch(err){_d=!0,_e=err}finally{try{_n||null==_i.return||_i.return()}finally{if(_d)throw _e}}return _arr}(arr,i)||function Login_unsupportedIterableToArray(o,minLen){if(!o)return;if("string"==typeof o)return Login_arrayLikeToArray(o,minLen);var n=Object.prototype.toString.call(o).slice(8,-1);"Object"===n&&o.constructor&&(n=o.constructor.name);if("Map"===n||"Set"===n)return Array.from(o);if("Arguments"===n||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n))return Login_arrayLikeToArray(o,minLen)}(arr,i)||function Login_nonIterableRest(){throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.")}()}function Login_arrayLikeToArray(arr,len){(null==len||len>arr.length)&&(len=arr.length);for(var i=0,arr2=Array(len);i<len;i++)arr2[i]=arr[i];return arr2}"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/modules/auth/components/PageLayout/AuthPageLayout.tsx"]={name:"AuthPageLayout",docgenInfo:AuthPageLayout.__docgenInfo,path:"src/modules/auth/components/PageLayout/AuthPageLayout.tsx"});var _ref2=react_default.a.createElement(head_default.a,null,react_default.a.createElement("title",null,"Insight | Sign up")),_ref3=react_default.a.createElement("img",{src:"data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGcgY2xpcC1wYXRoPSJ1cmwoI2NsaXAwKSI+CjxwYXRoIGQ9Ik0xNS45OTk3IDguMTg0MTdDMTUuOTk5NyA3LjY0MDM1IDE1Ljk1NDcgNy4wOTM1OSAxNS44NTg4IDYuNTU4NTlIOC4xNjAxNlY5LjYzOTI1SDEyLjU2ODhDMTIuMzg1OCAxMC42MzI4IDExLjc5OCAxMS41MTE3IDEwLjkzNzMgMTIuMDcwM1YxNC4wNjkySDEzLjU2NzVDMTUuMTEyIDEyLjY3NTggMTUuOTk5NyAxMC42MTgxIDE1Ljk5OTcgOC4xODQxN1oiIGZpbGw9IiM0Mjg1RjQiLz4KPHBhdGggZD0iTTguMTYwMTggMTYuMDAwMkMxMC4zNjE1IDE2LjAwMDIgMTIuMjE3OSAxNS4yOTE4IDEzLjU3MDUgMTQuMDY4OUwxMC45NDAzIDEyLjA3QzEwLjIwODUgMTIuNTU4IDkuMjYzODMgMTIuODM0MyA4LjE2MzE3IDEyLjgzNDNDNi4wMzM4NCAxMi44MzQzIDQuMjI4NCAxMS40MjYzIDMuNTgwNjEgOS41MzMySDAuODY2NDU1VjExLjU5MzhDMi4yNTIwMiAxNC4yOTUzIDUuMDc0MTQgMTYuMDAwMiA4LjE2MDE4IDE2LjAwMDJaIiBmaWxsPSIjMzRBODUzIi8+CjxwYXRoIGQ9Ik0zLjU3NzY3IDkuNTMzOEMzLjIzNTc4IDguNTQwMjMgMy4yMzU3OCA3LjQ2NDM1IDMuNTc3NjcgNi40NzA3OFY0LjQxMDE2SDAuODY2NTJDLTAuMjkxMTE5IDYuNjcwNjcgLTAuMjkxMTE5IDkuMzMzOTEgMC44NjY1MiAxMS41OTQ0TDMuNTc3NjcgOS41MzM4WiIgZmlsbD0iI0ZCQkMwNCIvPgo8cGF0aCBkPSJNOC4xNjAxOCAzLjE2NjQ0QzkuMzIzODEgMy4xNDg4IDEwLjQ0ODUgMy41Nzc5OCAxMS4yOTEyIDQuMzY1NzhMMTMuNjIxNSAyLjA4MTc0QzEyLjE0NTkgMC43MjM2NyAxMC4xODc1IC0wLjAyMjk3NzMgOC4xNjAxOCAwLjAwMDUzOTExMUM1LjA3NDE0IDAuMDAwNTM5MTExIDIuMjUyMDIgMS43MDU0OCAwLjg2NjQ1NSA0LjQwOTg3TDMuNTc3NjEgNi40NzA1QzQuMjIyNDEgNC41NzQ0OSA2LjAzMDg0IDMuMTY2NDQgOC4xNjAxOCAzLjE2NjQ0WiIgZmlsbD0iI0VBNDMzNSIvPgo8L2c+CjxkZWZzPgo8Y2xpcFBhdGggaWQ9ImNsaXAwIj4KPHBhdGggZD0iTTAgMEgxNlYxNkgwVjBaIiBmaWxsPSJ3aGl0ZSIvPgo8L2NsaXBQYXRoPgo8L2RlZnM+Cjwvc3ZnPgo=",alt:"Google Logo"}),_ref4=react_default.a.createElement(Divider_Divider,null),_ref5=react_default.a.createElement(block.a,{display:"flex",justifyContent:"space-between"},react_default.a.createElement("span",null,"Password"),react_default.a.createElement(link_default.a,{href:"/password-forgot"},react_default.a.createElement("a",null,"Forgot?"))),_ref6=react_default.a.createElement(Divider_Divider,null),_ref7=react_default.a.createElement(button_button.a,{kind:"minimal",size:"compact"},"Create a free account"),Login=function(){var _errors$email,_errors$password,router=Object(next_router.useRouter)(),_useState2=Login_slicedToArray(Object(react.useState)(!1),2),isSubmitting=_useState2[0],setIsSubmitting=_useState2[1],_useStyletron2=Login_slicedToArray(Object(styled.b)(),2),theme=(_useStyletron2[0],_useStyletron2[1]),_useForm=Object(react_hook_form_es.a)(),register=_useForm.register,handleSubmit=_useForm.handleSubmit,errors=_useForm.errors,inputOverrides=function(theme){var inputBorders=function(theme){return{borderBottomRightRadius:theme.sizing.scale100,borderTopRightRadius:theme.sizing.scale100,borderTopLeftRadius:theme.sizing.scale100,borderBottomLeftRadius:theme.sizing.scale100}}(theme);return{InputContainer:{style:inputBorders},Input:{style:inputBorders}}}(theme),_router$query$dest=router.query.dest,dest=void 0===_router$query$dest?encodeURIComponent("/"):_router$query$dest,_useState4=Login_slicedToArray(Object(react.useState)(),2),formError=_useState4[0],setFormError=_useState4[1],onSubmit=handleSubmit((function(formData){isSubmitting||(setIsSubmitting(!0),sso.login(formData.email,formData.password).then((function(){return router.replace(decodeURIComponent(dest))})).catch(function(){var _ref=function _asyncToGenerator(fn){return function(){var self=this,args=arguments;return new Promise((function(resolve,reject){var gen=fn.apply(self,args);function _next(value){asyncGeneratorStep(gen,resolve,reject,_next,_throw,"next",value)}function _throw(err){asyncGeneratorStep(gen,resolve,reject,_next,_throw,"throw",err)}_next(void 0)}))}}(regeneratorRuntime.mark((function _callee(error){var errorDTO;return regeneratorRuntime.wrap((function(_context){for(;;)switch(_context.prev=_context.next){case 0:return _context.next=2,error.response.json();case 2:errorDTO=_context.sent,setFormError(errorDTO.error);case 4:case"end":return _context.stop()}}),_callee)})));return function(){return _ref.apply(this,arguments)}}()).finally((function(){return setIsSubmitting(!1)})))}));return react_default.a.createElement(PageLayout_AuthPageLayout,null,_ref2,react_default.a.createElement("a",{href:"".concat(authApiBaseURL,"/v1/sso/google/signin?dest=").concat(dest),style:{textDecoration:"none"}},react_default.a.createElement(button_button.a,{$style:{width:"100%"},startEnhancer:_ref3,kind:"secondary"},"Sign in with Google")),_ref4,react_default.a.createElement("form",{onSubmit:onSubmit,noValidate:!0},react_default.a.createElement(block.a,null,react_default.a.createElement(form_control.a,{label:"Email",error:null===(_errors$email=errors.email)||void 0===_errors$email?void 0:_errors$email.message},react_default.a.createElement(input.a,{overrides:inputOverrides,name:"email",type:"email",placeholder:"Email",required:!0,inputRef:register(EMAIL_VALIDATION),error:!!errors.email}))),react_default.a.createElement(block.a,{marginBottom:theme.sizing.scale1200},react_default.a.createElement(form_control.a,{label:_ref5,error:null===(_errors$password=errors.password)||void 0===_errors$password?void 0:_errors$password.message},react_default.a.createElement(input.a,{overrides:inputOverrides,placeholder:"Password",name:"password",type:"password",ref:register,inputRef:register(PASSWORD_VALIDATION),error:!!errors.password}))),react_default.a.createElement(button_button.a,{type:"submit",$style:{width:"100%"},isLoading:isSubmitting},"Sign in"),formError&&react_default.a.createElement(FormError_FormError,{error:formError})),_ref6,react_default.a.createElement(block.a,null,react_default.a.createElement("a",{href:TRY_BASE_URL,style:{textDecoration:"none"}},_ref7),react_default.a.createElement(button_button.a,{kind:"minimal",size:"compact",$style:{marginLeft:theme.sizing.scale600}},"Join an existing team")))};Login.displayName="Login",Login.__docgenInfo={description:"",methods:[],displayName:"Login"};var Login_Login=Login;"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/modules/auth/components/Login/Login.tsx"]={name:"Login",docgenInfo:Login.__docgenInfo,path:"src/modules/auth/components/Login/Login.tsx"});__webpack_exports__.default={title:"Auth|Login"};var Login_stories_ref=react_default.a.createElement(Login_Login,null),Base=function(){return Login_stories_ref};Base.displayName="Base",Base.story=Object(dist.configureStory)({setupMocks:function setupMocks(sandbox){return sandbox.stub(sso,"login").callsFake((function(){return new Promise((function(resolve){return setTimeout(resolve,10)}))}))}});var Login_stories_ref2=react_default.a.createElement(Login_Login,null),InvalidPassword=function(){return Login_stories_ref2};InvalidPassword.displayName="InvalidPassword",InvalidPassword.story=Object(dist.configureStory)({setupMocks:function setupMocks(sandbox){return sandbox.stub(sso,"login").callsFake((function(){var error=Object(dist.mockApiError)({statusCode:400,reason:"Bad Request",message:"Invalid email or password"});return new Promise((function(_resolve,reject){return setTimeout((function(){return reject(error)}),10)}))}))}}),Base.__docgenInfo={description:"",methods:[],displayName:"Base"},"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/modules/auth/components/Login/Login.stories.tsx"]={name:"Base",docgenInfo:Base.__docgenInfo,path:"src/modules/auth/components/Login/Login.stories.tsx"}),InvalidPassword.__docgenInfo={description:"",methods:[],displayName:"InvalidPassword"},"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/modules/auth/components/Login/Login.stories.tsx"]={name:"InvalidPassword",docgenInfo:InvalidPassword.__docgenInfo,path:"src/modules/auth/components/Login/Login.stories.tsx"})},796:function(module,__webpack_exports__,__webpack_require__){"use strict";__webpack_require__.r(__webpack_exports__);var client=__webpack_require__(145),react=(__webpack_require__(11),__webpack_require__(74),__webpack_require__(0)),react_default=__webpack_require__.n(react),public_api=__webpack_require__(93),dist=__webpack_require__(41),router_context=__webpack_require__(225),next_router=__webpack_require__(146),browser_es5_es=__webpack_require__(111),dist_browser_es5_es=__webpack_require__(68),styletron="undefined"==typeof window?new browser_es5_es.b:new browser_es5_es.a({hydrate:document.getElementsByClassName("_styletron_hydrate_")}),base_provider=__webpack_require__(809),light_theme=__webpack_require__(799),ThemeProvider=function(_ref){var children=_ref.children;return react_default.a.createElement(base_provider.a,{theme:light_theme.a,overrides:{AppContainer:{style:{height:"100%",display:"flex",flexDirection:"column"}}}},children)};ThemeProvider.displayName="ThemeProvider",ThemeProvider.__docgenInfo={description:"",methods:[],displayName:"ThemeProvider",props:{children:{required:!0,tsType:{name:"JSX.Element"},description:""}}};var ThemeProvider_ThemeProvider=ThemeProvider;"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/shared/containers/ThemeProvider/ThemeProvider.tsx"]={name:"ThemeProvider",docgenInfo:ThemeProvider.__docgenInfo,path:"src/shared/containers/ThemeProvider/ThemeProvider.tsx"});var AppProviders=function(_ref){var children=_ref.children,_ref$engine=_ref.engine,engine=void 0===_ref$engine?styletron:_ref$engine;return react_default.a.createElement(dist_browser_es5_es.a,{value:engine,debug:void 0,debugAfterHydration:!0},react_default.a.createElement(ThemeProvider_ThemeProvider,null,children))};AppProviders.displayName="AppProviders",AppProviders.__docgenInfo={description:"",methods:[],displayName:"AppProviders",props:{engine:{defaultValue:{value:"styletron",computed:!0},required:!1,tsType:{name:"union",raw:"Client | Server",elements:[{name:"Client"},{name:"Server"}]},description:""},children:{required:!0,tsType:{name:"JSX.Element"},description:""}}};var AppProviders_AppProviders=AppProviders;"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/shared/containers/AppProviders/AppProviders.tsx"]={name:"AppProviders",docgenInfo:AppProviders.__docgenInfo,path:"src/shared/containers/AppProviders/AppProviders.tsx"});var nextDecorator_engine=new browser_es5_es.a,_ref2=react_default.a.createElement("div",null,"Hello world"),nextDecorator=Object(public_api.makeDecorator)({name:"nextDecorator",parameterName:"next__decorator",skipIfNoParametersOrOptions:!1,wrapper:function wrapper(story,context,_ref){_ref.parameters;var router={route:"/",pathname:"/",query:{},asPath:"/",basePath:"/",push:function push(){for(var _len=arguments.length,args=Array(_len),_key=0;_key<_len;_key++)args[_key]=arguments[_key];return Object(dist.action)("push")(args),Promise.resolve(!0)},back:Object(dist.action)("back"),replace:function replace(){for(var _len2=arguments.length,args=Array(_len2),_key2=0;_key2<_len2;_key2++)args[_key2]=arguments[_key2];return Object(dist.action)("replace")(args),Promise.resolve(!0)},reload:Object(dist.action)("reload"),beforePopState:Object(dist.action)("beforePopState"),prefetch:function prefetch(){for(var _len3=arguments.length,args=Array(_len3),_key3=0;_key3<_len3;_key3++)args[_key3]=arguments[_key3];return Object(dist.action)("prefetch")(args),Promise.resolve()},events:{on:Object(dist.action)("events.on"),off:Object(dist.action)("events.off"),emit:Object(dist.action)("events.emit")},isFallback:!1};return Object(next_router.createRouter)(router.pathname,router.query,router.asPath,{isFallback:router.isFallback,pageLoader:{loadPage:function loadPage(){return Object(dist.action)("loadPage").apply(void 0,arguments),Promise.resolve((function(){return _ref2}))}}}),react_default.a.createElement(AppProviders_AppProviders,{engine:nextDecorator_engine},react_default.a.createElement(router_context.RouterContext.Provider,{value:router},story(context)))}});Object(client.addDecorator)(nextDecorator)},797:function(module,__webpack_exports__,__webpack_require__){"use strict";__webpack_require__.r(__webpack_exports__),__webpack_require__.d(__webpack_exports__,"Base",(function(){return Base}));var react=__webpack_require__(0),react_default=__webpack_require__.n(react),styled=(__webpack_require__(21),__webpack_require__(24),__webpack_require__(25),__webpack_require__(34),__webpack_require__(28),__webpack_require__(15),__webpack_require__(85),__webpack_require__(65),__webpack_require__(29),__webpack_require__(47),__webpack_require__(11),__webpack_require__(30),__webpack_require__(20),__webpack_require__(22),__webpack_require__(226)),input=__webpack_require__(812),block=__webpack_require__(16);function _slicedToArray(arr,i){return function _arrayWithHoles(arr){if(Array.isArray(arr))return arr}(arr)||function _iterableToArrayLimit(arr,i){if("undefined"==typeof Symbol||!(Symbol.iterator in Object(arr)))return;var _arr=[],_n=!0,_d=!1,_e=void 0;try{for(var _s,_i=arr[Symbol.iterator]();!(_n=(_s=_i.next()).done)&&(_arr.push(_s.value),!i||_arr.length!==i);_n=!0);}catch(err){_d=!0,_e=err}finally{try{_n||null==_i.return||_i.return()}finally{if(_d)throw _e}}return _arr}(arr,i)||function _unsupportedIterableToArray(o,minLen){if(!o)return;if("string"==typeof o)return _arrayLikeToArray(o,minLen);var n=Object.prototype.toString.call(o).slice(8,-1);"Object"===n&&o.constructor&&(n=o.constructor.name);if("Map"===n||"Set"===n)return Array.from(o);if("Arguments"===n||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n))return _arrayLikeToArray(o,minLen)}(arr,i)||function _nonIterableRest(){throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.")}()}function _arrayLikeToArray(arr,len){(null==len||len>arr.length)&&(len=arr.length);for(var i=0,arr2=Array(len);i<len;i++)arr2[i]=arr[i];return arr2}var hooks_useElementState=function useElementState(_ref){var on=_ref.on,off=_ref.off,_ref$initialState=_ref.initialState,initialState=void 0!==_ref$initialState&&_ref$initialState,_useState2=_slicedToArray(Object(react.useState)(initialState),2),value=_useState2[0],setValue=_useState2[1],ref=Object(react.useRef)(null),setOn=Object(react.useCallback)((function(){return setValue(!0)}),[]),setOff=Object(react.useCallback)((function(){return setValue(!1)}),[]);return[value,Object(react.useCallback)((function(node){null!=ref.current&&(ref.current.removeEventListener(on,setOn),ref.current.removeEventListener(off,setOff)),ref.current=node,null!=ref.current&&(ref.current.addEventListener(on,setOn),ref.current.addEventListener(off,setOff))}),[on,off,setOn,setOff,ref]),ref]};var hooks_useFocus=function useFocus(){return hooks_useElementState({on:"focus",off:"blur"})},search=__webpack_require__(810),tag=__webpack_require__(814),popover=__webpack_require__(811);function GlobalSearch_slicedToArray(arr,i){return function GlobalSearch_arrayWithHoles(arr){if(Array.isArray(arr))return arr}(arr)||function GlobalSearch_iterableToArrayLimit(arr,i){if("undefined"==typeof Symbol||!(Symbol.iterator in Object(arr)))return;var _arr=[],_n=!0,_d=!1,_e=void 0;try{for(var _s,_i=arr[Symbol.iterator]();!(_n=(_s=_i.next()).done)&&(_arr.push(_s.value),!i||_arr.length!==i);_n=!0);}catch(err){_d=!0,_e=err}finally{try{_n||null==_i.return||_i.return()}finally{if(_d)throw _e}}return _arr}(arr,i)||function GlobalSearch_unsupportedIterableToArray(o,minLen){if(!o)return;if("string"==typeof o)return GlobalSearch_arrayLikeToArray(o,minLen);var n=Object.prototype.toString.call(o).slice(8,-1);"Object"===n&&o.constructor&&(n=o.constructor.name);if("Map"===n||"Set"===n)return Array.from(o);if("Arguments"===n||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n))return GlobalSearch_arrayLikeToArray(o,minLen)}(arr,i)||function GlobalSearch_nonIterableRest(){throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.")}()}function GlobalSearch_arrayLikeToArray(arr,len){(null==len||len>arr.length)&&(len=arr.length);for(var i=0,arr2=Array(len);i<len;i++)arr2[i]=arr[i];return arr2}var KEY_CODE_ESC=27,KEY_CODE_SLASH=191,GlobalSearch_ref=react_default.a.createElement(search.a,null),_ref2=react_default.a.createElement(tag.a,{closeable:!1},"/"),GlobalSearch=function(){var _useStyletron2=GlobalSearch_slicedToArray(Object(styled.b)(),2),theme=(_useStyletron2[0],_useStyletron2[1]),_useFocus2=GlobalSearch_slicedToArray(hooks_useFocus(),3),active=_useFocus2[0],inputRefCallback=_useFocus2[1],inputRef=_useFocus2[2],width=active?420:280,focus=Object(react.useCallback)((function(){inputRef.current&&inputRef.current.focus()}),[inputRef]),blur=Object(react.useCallback)((function(){inputRef.current&&inputRef.current.blur()}),[inputRef]);Object(react.useEffect)((function(){var onKeyDown=function(event){event.keyCode===KEY_CODE_SLASH?(event.stopPropagation(),event.preventDefault(),focus()):event.keyCode===KEY_CODE_ESC&&blur()};return document.addEventListener("keydown",onKeyDown),function(){document.removeEventListener("keydown",onKeyDown)}}));var content=react_default.a.createElement("ul",{style:{width:width,margin:0}},[1,2,3,4,5].map((function(item){return react_default.a.createElement("li",{onMouseDown:function onMouseDown(event){event.preventDefault()},onClick:blur,key:item,style:{padding:16,margin:0,cursor:"pointer",listStyle:"none"}},"item")})));return react_default.a.createElement(popover.a,{isOpen:active,content:content,placement:"bottomLeft"},react_default.a.createElement(block.a,{marginLeft:theme.sizing.scale600},react_default.a.createElement(input.a,{inputRef:inputRefCallback,placeholder:"Search insights...",size:"mini",startEnhancer:GlobalSearch_ref,endEnhancer:_ref2,overrides:{StartEnhancer:{style:{borderTopLeftRadius:theme.sizing.scale100,borderBottomLeftRadius:theme.sizing.scale100}},EndEnhancer:{style:{borderBottomRightRadius:theme.sizing.scale100,borderTopRightRadius:theme.sizing.scale100}},InputContainer:{style:{width:"".concat(width-38,"px"),transitionTimingFunction:"ease",transitionDuration:"0.2s",transitionProperty:"width"}}}})))};GlobalSearch.displayName="GlobalSearch",GlobalSearch.__docgenInfo={description:"",methods:[],displayName:"GlobalSearch"};var GlobalSearch_GlobalSearch=react_default.a.memo(GlobalSearch);"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/modules/app/components/GlobalSearch/GlobalSearch.tsx"]={name:"GlobalSearch",docgenInfo:GlobalSearch.__docgenInfo,path:"src/modules/app/components/GlobalSearch/GlobalSearch.tsx"});__webpack_exports__.default={title:"App|GlobalSearch"};var GlobalSearch_stories_ref=react_default.a.createElement(GlobalSearch_GlobalSearch,null),Base=function(){return GlobalSearch_stories_ref};Base.displayName="Base",Base.__docgenInfo={description:"",methods:[],displayName:"Base"},"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/modules/app/components/GlobalSearch/GlobalSearch.stories.tsx"]={name:"Base",docgenInfo:Base.__docgenInfo,path:"src/modules/app/components/GlobalSearch/GlobalSearch.stories.tsx"})}},[[375,1,2]]]);
//# sourceMappingURL=main.471e21c6399a5cd2ec3f.bundle.js.map