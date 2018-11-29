'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _reactDom = require('react-dom');

var _reactDom2 = _interopRequireDefault(_reactDom);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _classnames = require('classnames');

var _classnames2 = _interopRequireDefault(_classnames);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _objectWithoutProperties(obj, keys) { var target = {}; for (var i in obj) { if (keys.indexOf(i) >= 0) continue; if (!Object.prototype.hasOwnProperty.call(obj, i)) continue; target[i] = obj[i]; } return target; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var canUseDOM = !!(typeof window !== 'undefined' && window.document && window.document.createElement);

var lazySizes = null;

if (canUseDOM) {
  lazySizes = require('lazysizes');
}

var LazySizes = function (_React$Component) {
  _inherits(LazySizes, _React$Component);

  function LazySizes() {
    var _ref;

    var _temp, _this, _ret;

    _classCallCheck(this, LazySizes);

    for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }

    return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = LazySizes.__proto__ || Object.getPrototypeOf(LazySizes)).call.apply(_ref, [this].concat(args))), _this), _this.componentWillMount = function () {
      var _this$props = _this.props,
          iframe = _this$props.iframe,
          dataSrc = _this$props.dataSrc;

      if (iframe && !dataSrc) {
        (0, _invariant2.default)(false, 'Prop dataSrc is required on iframe.');
      }
    }, _this.componentWillUpdate = function (nextProps) {
      var propsChanged = false;
      var _arr = ['src', 'dataSizes', 'dataSrc', 'dataSrcSet', 'className', 'iframe'];
      for (var _i = 0; _i < _arr.length; _i++) {
        var propName = _arr[_i];
        var prop = propName === 'dataSrcSet' ? _this.handleSrcSet(_this.props[propName]) : _this.props[propName];
        var nextProp = propName === 'dataSrcSet' ? _this.handleSrcSet(nextProps[propName]) : nextProps[propName];
        if (prop !== nextProp) {
          propsChanged = true;
          break;
        }
      }
      if (propsChanged && lazySizes) {
        var lazyElement = _reactDom2.default.findDOMNode(_this);
        if (lazySizes.hC(lazyElement, 'lazyloaded')) {
          lazySizes.rC(lazyElement, 'lazyloaded');
        }
      }
    }, _this.componentDidUpdate = function () {
      if (!lazySizes) {
        return;
      }
      var lazyElement = _reactDom2.default.findDOMNode(_this);
      if (!lazySizes.hC(lazyElement, 'lazyloaded') && !lazySizes.hC(lazyElement, 'lazyload')) {
        lazySizes.aC(lazyElement, 'lazyload');
      }
    }, _this.handleSrcSet = function (srcSet) {
      var result = srcSet;
      if ((typeof srcSet === 'undefined' ? 'undefined' : _typeof(srcSet)) === 'object') {
        if (!Array.isArray(srcSet)) {
          result = [];
          for (var variant in srcSet) {
            if (srcSet.hasOwnProperty(variant)) {
              result.push({
                variant: variant,
                src: srcSet[variant]
              });
            }
          }
        }
        result = result.map(function (item) {
          return item.src + ' ' + item.variant;
        }).join(', ');
      }
      return result;
    }, _temp), _possibleConstructorReturn(_this, _ret);
  }

  _createClass(LazySizes, [{
    key: 'render',
    value: function render() {
      var _props = this.props,
          src = _props.src,
          dataSizes = _props.dataSizes,
          dataSrc = _props.dataSrc,
          dataSrcSet = _props.dataSrcSet,
          className = _props.className,
          iframe = _props.iframe,
          other = _objectWithoutProperties(_props, ['src', 'dataSizes', 'dataSrc', 'dataSrcSet', 'className', 'iframe']);

      dataSrcSet = this.handleSrcSet(dataSrcSet);
      className = (0, _classnames2.default)(['lazyload', className]).trim();
      if (iframe) {
        return _react2.default.createElement('iframe', _extends({}, other, {
          src: dataSrc ? '' : src,
          'data-src': dataSrc,
          className: className }));
      }
      return _react2.default.createElement('img', _extends({}, other, {
        src: src,
        'data-src': dataSrc,
        'data-sizes': dataSizes,
        'data-srcset': dataSrcSet,
        className: className }));
    }
  }]);

  return LazySizes;
}(_react2.default.Component);

LazySizes.propTypes = {
  src: _propTypes2.default.string,
  dataSizes: _propTypes2.default.string,
  dataSrc: _propTypes2.default.string,
  dataSrcSet: _propTypes2.default.oneOfType([_propTypes2.default.string, _propTypes2.default.object, _propTypes2.default.array]),
  className: _propTypes2.default.string,
  iframe: _propTypes2.default.bool
};
LazySizes.defaultProps = {
  src: 'data:image/gif;base64,R0lGODdhEAAJAIAAAMLCwsLCwiwAAAAAEAAJAAACCoSPqcvtD6OclBUAOw==',
  dataSizes: 'auto',
  iframe: false
};
exports.default = LazySizes;