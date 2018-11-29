"use strict";

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var environments = [{
  name: "None",
  path: null,
  format: ".jpg"
  // todo get some environments to play around with
  // {
  //   name: "Park (Day)",
  //   path: "assets/environment/Park2/",
  //   format: ".jpg"
  // },
  // {
  //   name: "Park (Night)",
  //   path: "assets/environment/Park3Med/",
  //   format: ".jpg"
  // },
  // {
  //   name: "Bridge",
  //   path: "assets/environment/Bridge2/",
  //   format: ".jpg"
  // },
  // {
  //   name: "Sky",
  //   path: "assets/environment/skybox/",
  //   format: ".jpg"
  // },
  // {
  //   name: "Castle",
  //   path: "assets/environment/SwedishRoyalCastle/",
  //   format: ".jpg"
  // },
  // {
  //   name: "Footprint Court (HDR)",
  //   path: "assets/environment/FootprintCourt/",
  //   format: ".hdr"
  // }
}];

var DEFAULT_CAMERA = "[default]";

var IS_IOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream;

// glTF texture types. `envMap` is deliberately omitted, as it's used internally
// by the loader but not part of the glTF format.
var MAP_NAMES = ["map", "aoMap", "emissiveMap", "glossinessMap", "metalnessMap", "normalMap", "roughnessMap", "specularMap"];

var Preset = { ASSET_GENERATOR: "assetgenerator" };

var Viewer = function () {
  function Viewer(el, options, THREE, dat, Stats, xhr) {
    _classCallCheck(this, Viewer);

    this.dat = dat;
    this.xhr = xhr;
    this.Statser = Stats;
    this.el = el;
    window.THREE = THREE;
    this.options = options;

    this.lights = [];
    this.content = null;
    this.mixer = null;
    this.clips = [];
    this.gui = null;

    this.state = {
      environment: options.preset === Preset.ASSET_GENERATOR ? "Footprint Court (HDR)" : environments[0].name,
      background: false,
      playbackSpeed: 1.0,
      actionStates: {},
      camera: DEFAULT_CAMERA,
      wireframe: false,
      skeleton: false,
      grid: false,

      // Lights
      addLights: true,
      exposure: 1.0,
      textureEncoding: "sRGB",
      ambientIntensity: 0.3,
      ambientColor: 0xffffff,
      directIntensity: 0.8,
      directColor: 0xffffff,
      bgColor1: "#ffffff",
      bgColor2: "#353535"
    };

    this.prevTime = 0;

    this.stats = new this.Statser();
    this.stats.dom.height = "48px";
    [].forEach.call(this.stats.dom.children, function (child) {
      return child.style.display = "";
    });

    this.scene = new THREE.Scene();

    var fov = options.preset === Preset.ASSET_GENERATOR ? 0.8 * 180 / Math.PI : 60;
    this.defaultCamera = new THREE.PerspectiveCamera(fov, el.clientWidth / el.clientHeight, 0.01, 1000);
    this.activeCamera = this.defaultCamera;
    this.scene.add(this.defaultCamera);

    this.renderer = window.renderer = new THREE.WebGLRenderer({
      antialias: true
    });
    this.renderer.gammaOutput = true;
    this.renderer.gammaFactor = 2.2;
    this.renderer.setClearColor(0xcccccc);
    this.renderer.setPixelRatio(window.devicePixelRatio);
    this.renderer.setSize(el.clientWidth, el.clientHeight);

    this.controls = new THREE.OrbitControls(this.defaultCamera, this.renderer.domElement);
    this.controls.autoRotate = false;
    this.controls.autoRotateSpeed = -10;
    this.controls.screenSpacePanning = true;

    this.el.appendChild(this.renderer.domElement);

    this.cameraCtrl = null;
    this.cameraFolder = null;
    this.animFolder = null;
    this.animCtrls = [];
    this.morphFolder = null;
    this.morphCtrls = [];
    this.skeletonHelpers = [];
    this.gridHelper = null;
    this.axesHelper = null;

    this.addGUI();
    if (options.kiosk) this.gui.close();

    this.animate = this.animate.bind(this);
    requestAnimationFrame(this.animate);
    window.addEventListener("resize", this.resize.bind(this), false);
  }

  _createClass(Viewer, [{
    key: "animate",
    value: function animate(time) {
      requestAnimationFrame(this.animate);

      var dt = (time - this.prevTime) / 1000;

      this.controls.update();
      this.stats.update();
      this.mixer && this.mixer.update(dt);
      this.render();

      this.prevTime = time;
    }
  }, {
    key: "render",
    value: function render() {
      this.renderer.render(this.scene, this.activeCamera);
    }
  }, {
    key: "resize",
    value: function resize() {
      var _el$parentElement = this.el.parentElement,
          clientHeight = _el$parentElement.clientHeight,
          clientWidth = _el$parentElement.clientWidth,
          width = _el$parentElement.width,
          height = _el$parentElement.height;


      this.defaultCamera.aspect = clientWidth / clientHeight;
      this.defaultCamera.updateProjectionMatrix();
      // this.background.style({ aspect: this.defaultCamera.aspect });
      this.renderer.setSize(clientWidth, clientHeight, false);
    }
  }, {
    key: "load",
    value: function load(url, rootPath, assetMap) {
      var _this = this;

      var baseURL = THREE.LoaderUtils.extractUrlBase(url);

      // Load.
      return new Promise(function (resolve, reject) {
        var manager = new THREE.LoadingManager();

        // Intercept and override relative URLs.
        manager.setURLModifier(function (url, path) {
          var normalizedURL = rootPath + url.replace(baseURL, "").replace(/^(\.?\/)/, "");

          if (assetMap.has(normalizedURL)) {
            var blob = assetMap.get(normalizedURL);
            var blobURL = URL.createObjectURL(blob);
            blobURLs.push(blobURL);
            return blobURL;
          }

          return (path || "") + url;
        });

        var loader = new THREE.GLTFLoader(manager);
        loader.setCrossOrigin("anonymous");
        loader.setDRACOLoader(new THREE.DRACOLoader());
        var blobURLs = [];

        loader.load(url, function (gltf) {
          var scene = gltf.scene || gltf.scenes[0];
          var clips = gltf.animations || [];
          _this.setContent(scene, clips);

          blobURLs.forEach(URL.revokeObjectURL);

          // See: https://github.com/google/draco/issues/349
          // THREE.DRACOLoader.releaseDecoderModule();
          _this.xhr({ loaded: 2, total: 1 });
          resolve();
        }, _this.xhr, reject);
      });
    }

    /**
     * @param {THREE.Object3D} object
     * @param {Array<THREE.AnimationClip} clips
     */

  }, {
    key: "setContent",
    value: function setContent(object, clips) {
      var _this2 = this;

      this.clear();

      object.updateMatrixWorld();
      var box = new THREE.Box3().setFromObject(object);
      var size = box.getSize(new THREE.Vector3()).length();
      var center = box.getCenter(new THREE.Vector3());

      this.controls.reset();

      object.position.x += object.position.x - center.x;
      object.position.y += object.position.y - center.y;
      object.position.z += object.position.z - center.z;
      this.controls.maxDistance = size * 10;
      this.defaultCamera.near = size / 100;
      this.defaultCamera.far = size * 100;
      this.defaultCamera.updateProjectionMatrix();

      if (this.options.cameraPosition) {
        this.defaultCamera.position.fromArray(this.options.cameraPosition);
        this.defaultCamera.lookAt(new THREE.Vector3());
      } else {
        this.defaultCamera.position.copy(center);
        this.defaultCamera.position.x += size / 2.0;
        this.defaultCamera.position.y += size / 5.0;
        this.defaultCamera.position.z += size / 2.0;
        this.defaultCamera.lookAt(center);
      }

      this.setCamera(DEFAULT_CAMERA);

      this.controls.saveState();

      this.scene.add(object);
      this.content = object;

      this.state.addLights = true;
      this.content.traverse(function (node) {
        if (node.isLight) {
          _this2.state.addLights = false;
        }
      });

      this.setClips(clips);

      this.updateLights();
      this.updateGUI();
      this.updateEnvironment();
      this.updateTextureEncoding();
      this.updateDisplay();

      window.content = this.content;
      console.info("[glTF Viewer] THREE.Scene exported as `window.content`.");
      this.printGraph(this.content);
    }
  }, {
    key: "printGraph",
    value: function printGraph(node) {
      var _this3 = this;

      console.group(" <" + node.type + "> " + node.name);
      node.children.forEach(function (child) {
        return _this3.printGraph(child);
      });
      console.groupEnd();
    }

    /**
     * @param {Array<THREE.AnimationClip} clips
     */

  }, {
    key: "setClips",
    value: function setClips(clips) {
      if (this.mixer) {
        this.mixer.stopAllAction();
        this.mixer.uncacheRoot(this.mixer.getRoot());
        this.mixer = null;
      }

      clips.forEach(function (clip) {
        if (clip.validate()) clip.optimize();
      });

      this.clips = clips;
      if (!clips.length) return;

      this.mixer = new THREE.AnimationMixer(this.content);
    }
  }, {
    key: "playAllClips",
    value: function playAllClips() {
      var _this4 = this;

      this.clips.forEach(function (clip) {
        _this4.mixer.clipAction(clip).reset().play();
        _this4.state.actionStates[clip.name] = true;
      });
    }

    /**
     * @param {string} name
     */

  }, {
    key: "setCamera",
    value: function setCamera(name) {
      var _this5 = this;

      if (name === DEFAULT_CAMERA) {
        this.controls.enabled = true;
        this.activeCamera = this.defaultCamera;
      } else {
        this.controls.enabled = false;
        this.content.traverse(function (node) {
          if (node.isCamera && node.name === name) {
            _this5.activeCamera = node;
          }
        });
      }
    }
  }, {
    key: "updateTextureEncoding",
    value: function updateTextureEncoding() {
      var encoding = this.state.textureEncoding === "sRGB" ? THREE.sRGBEncoding : THREE.LinearEncoding;
      traverseMaterials(this.content, function (material) {
        if (material.map) material.map.encoding = encoding;
        if (material.emissiveMap) material.emissiveMap.encoding = encoding;
        if (material.map || material.emissiveMap) material.needsUpdate = true;
      });
    }
  }, {
    key: "updateLights",
    value: function updateLights() {
      var state = this.state;
      var lights = this.lights;

      if (state.addLights && !lights.length) {
        this.addLights();
      } else if (!state.addLights && lights.length) {
        this.removeLights();
      }

      this.renderer.toneMappingExposure = state.exposure;

      if (lights.length === 2) {
        lights[0].intensity = state.ambientIntensity;
        lights[0].color.setHex(state.ambientColor);
        lights[1].intensity = state.directIntensity;
        lights[1].color.setHex(state.directColor);
      }
    }
  }, {
    key: "addLights",
    value: function addLights() {
      var state = this.state;

      if (this.options.preset === Preset.ASSET_GENERATOR) {
        var hemiLight = new THREE.HemisphereLight();
        hemiLight.name = "hemi_light";
        this.scene.add(hemiLight);
        this.lights.push(hemiLight);
        return;
      }

      var light1 = new THREE.AmbientLight(state.ambientColor, state.ambientIntensity);
      light1.name = "ambient_light";
      this.defaultCamera.add(light1);

      var light2 = new THREE.DirectionalLight(state.directColor, state.directIntensity);
      light2.position.set(0.5, 0, 0.866); // ~60º
      light2.name = "main_light";
      this.defaultCamera.add(light2);

      this.lights.push(light1, light2);
    }
  }, {
    key: "removeLights",
    value: function removeLights() {
      this.lights.forEach(function (light) {
        return light.parent.remove(light);
      });
      this.lights.length = 0;
    }
  }, {
    key: "updateEnvironment",
    value: function updateEnvironment() {
      var _this6 = this;

      var environment = environments.filter(function (entry) {
        return entry.name === _this6.state.environment;
      })[0];

      this.getCubeMapTexture(environment).then(function (texture) {
        if ((!texture || !_this6.state.background) && _this6.activeCamera === _this6.defaultCamera) {
          // this.scene.add(this.background);
        }

        traverseMaterials(_this6.content, function (material) {
          if (material.isMeshStandardMaterial || material.isGLTFSpecularGlossinessMaterial) {
            material.envMap = texture;
            material.needsUpdate = true;
          }
        });

        _this6.scene.background = _this6.state.background ? texture : null;
      });
    }
  }, {
    key: "getCubeMapTexture",
    value: function getCubeMapTexture(environment) {
      var path = environment.path,
          format = environment.format;

      // no envmap

      if (!path) return Promise.resolve();

      // standard
      var envMap = new THREE.CubeTextureLoader().load([path + "posx" + format, path + "negx" + format, path + "posy" + format, path + "negy" + format, path + "posz" + format, path + "negz" + format]);
      envMap.format = THREE.RGBFormat;
      return Promise.resolve(envMap);
    }
  }, {
    key: "updateDisplay",
    value: function updateDisplay() {
      var _this7 = this;

      if (this.skeletonHelpers.length) {
        this.skeletonHelpers.forEach(function (helper) {
          return _this7.scene.remove(helper);
        });
      }

      traverseMaterials(this.content, function (material) {
        material.wireframe = _this7.state.wireframe;
      });

      this.content.traverse(function (node) {
        if (node.isMesh && node.skeleton && _this7.state.skeleton) {
          var helper = new THREE.SkeletonHelper(node.skeleton.bones[0].parent);
          helper.material.linewidth = 3;
          _this7.scene.add(helper);
          _this7.skeletonHelpers.push(helper);
        }
      });

      if (this.state.grid !== Boolean(this.gridHelper)) {
        if (this.state.grid) {
          this.gridHelper = new THREE.GridHelper();
          this.axesHelper = new THREE.AxesHelper();
          this.axesHelper.renderOrder = 999;
          this.axesHelper.onBeforeRender = function (renderer) {
            return renderer.clearDepth();
          };
          this.scene.add(this.gridHelper);
          this.scene.add(this.axesHelper);
        } else {
          this.scene.remove(this.gridHelper);
          this.scene.remove(this.axesHelper);
          this.gridHelper = null;
          this.axesHelper = null;
        }
      }
    }
  }, {
    key: "updateBackground",
    value: function updateBackground() {
      // this.background.style({
      //   colors: [this.state.bgColor1, this.state.bgColor2]
      // });
    }
  }, {
    key: "addGUI",
    value: function addGUI() {
      var _this8 = this;

      var gui = this.gui = new this.dat.GUI({ autoPlace: false, width: 260 });

      // Display controls.
      var dispFolder = gui.addFolder("Display");
      var envBackgroundCtrl = dispFolder.add(this.state, "background");
      envBackgroundCtrl.onChange(function () {
        return _this8.updateEnvironment();
      });
      var wireframeCtrl = dispFolder.add(this.state, "wireframe");
      wireframeCtrl.onChange(function () {
        return _this8.updateDisplay();
      });
      var skeletonCtrl = dispFolder.add(this.state, "skeleton");
      skeletonCtrl.onChange(function () {
        return _this8.updateDisplay();
      });
      var gridCtrl = dispFolder.add(this.state, "grid");
      gridCtrl.onChange(function () {
        return _this8.updateDisplay();
      });
      dispFolder.add(this.controls, "autoRotate");
      dispFolder.add(this.controls, "screenSpacePanning");
      var bgColor1Ctrl = dispFolder.addColor(this.state, "bgColor1");
      var bgColor2Ctrl = dispFolder.addColor(this.state, "bgColor2");
      bgColor1Ctrl.onChange(function () {
        return _this8.updateBackground();
      });
      bgColor2Ctrl.onChange(function () {
        return _this8.updateBackground();
      });

      // Lighting controls.
      var lightFolder = gui.addFolder("Lighting");
      var encodingCtrl = lightFolder.add(this.state, "textureEncoding", ["sRGB", "Linear"]);
      encodingCtrl.onChange(function () {
        return _this8.updateTextureEncoding();
      });
      lightFolder.add(this.renderer, "gammaOutput").onChange(function () {
        traverseMaterials(_this8.content, function (material) {
          material.needsUpdate = true;
        });
      });
      var envMapCtrl = lightFolder.add(this.state, "environment", environments.map(function (env) {
        return env.name;
      }));
      envMapCtrl.onChange(function () {
        return _this8.updateEnvironment();
      });
      [lightFolder.add(this.state, "exposure", 0, 2), lightFolder.add(this.state, "addLights").listen(), lightFolder.add(this.state, "ambientIntensity", 0, 2), lightFolder.addColor(this.state, "ambientColor"), lightFolder.add(this.state, "directIntensity", 0, 2), lightFolder.addColor(this.state, "directColor")].forEach(function (ctrl) {
        return ctrl.onChange(function () {
          return _this8.updateLights();
        });
      });

      // Animation controls.
      this.animFolder = gui.addFolder("Animation");
      this.animFolder.domElement.style.display = "none";
      var playbackSpeedCtrl = this.animFolder.add(this.state, "playbackSpeed", 0, 1);
      playbackSpeedCtrl.onChange(function (speed) {
        if (_this8.mixer) _this8.mixer.timeScale = speed;
      });
      this.animFolder.add({ playAll: function playAll() {
          return _this8.playAllClips();
        } }, "playAll");

      // Morph target controls.
      this.morphFolder = gui.addFolder("Morph Targets");
      this.morphFolder.domElement.style.display = "none";

      // Camera controls.
      this.cameraFolder = gui.addFolder("Cameras");
      this.cameraFolder.domElement.style.display = "none";

      // Stats.
      var perfFolder = gui.addFolder("Performance");
      var perfLi = document.createElement("li");
      this.stats.dom.style.position = "static";
      perfLi.appendChild(this.stats.dom);
      perfLi.classList.add("gui-stats");
      perfFolder.__ul.appendChild(perfLi);

      var guiWrap = document.createElement("div");
      this.el.appendChild(guiWrap);
      guiWrap.classList.add("gui-wrap");
      guiWrap.appendChild(gui.domElement);
      gui.open();
    }
  }, {
    key: "updateGUI",
    value: function updateGUI() {
      var _this9 = this;

      this.cameraFolder.domElement.style.display = "none";

      this.morphCtrls.forEach(function (ctrl) {
        return ctrl.remove();
      });
      this.morphCtrls.length = 0;
      this.morphFolder.domElement.style.display = "none";

      this.animCtrls.forEach(function (ctrl) {
        return ctrl.remove();
      });
      this.animCtrls.length = 0;
      this.animFolder.domElement.style.display = "none";

      var cameraNames = [];
      var morphMeshes = [];
      this.content.traverse(function (node) {
        if (node.isMesh && node.morphTargetInfluences) {
          morphMeshes.push(node);
        }
        if (node.isCamera) {
          node.name = node.name || "VIEWER__camera_" + (cameraNames.length + 1);
          cameraNames.push(node.name);
        }
      });

      if (cameraNames.length) {
        this.cameraFolder.domElement.style.display = "";
        if (this.cameraCtrl) this.cameraCtrl.remove();
        var cameraOptions = [DEFAULT_CAMERA].concat(cameraNames);
        this.cameraCtrl = this.cameraFolder.add(this.state, "camera", cameraOptions);
        this.cameraCtrl.onChange(function (name) {
          return _this9.setCamera(name);
        });
      }

      if (morphMeshes.length) {
        this.morphFolder.domElement.style.display = "";
        morphMeshes.forEach(function (mesh) {
          if (mesh.morphTargetInfluences.length) {
            var nameCtrl = _this9.morphFolder.add({ name: mesh.name || "Untitled" }, "name");
            _this9.morphCtrls.push(nameCtrl);
          }
          for (var i = 0; i < mesh.morphTargetInfluences.length; i++) {
            var ctrl = _this9.morphFolder.add(mesh.morphTargetInfluences, i, 0, 1, 0.01).listen();
            _this9.morphCtrls.push(ctrl);
          }
        });
      }

      if (this.clips.length) {
        this.animFolder.domElement.style.display = "";
        var actionStates = this.state.actionStates = {};
        this.clips.forEach(function (clip, clipIndex) {
          // Autoplay the first clip.
          var action = void 0;
          if (clipIndex === 0) {
            actionStates[clip.name] = true;
            action = _this9.mixer.clipAction(clip);
            action.play();
          } else {
            actionStates[clip.name] = false;
          }

          // Play other clips when enabled.
          var ctrl = _this9.animFolder.add(actionStates, clip.name).listen();
          ctrl.onChange(function (playAnimation) {
            action = action || _this9.mixer.clipAction(clip);
            action.setEffectiveTimeScale(1);
            playAnimation ? action.play() : action.stop();
          });
          _this9.animCtrls.push(ctrl);
        });
      }
    }
  }, {
    key: "clear",
    value: function clear() {
      if (!this.content) return;

      this.scene.remove(this.content);

      // dispose geometry
      this.content.traverse(function (node) {
        if (!node.isMesh) return;

        node.geometry.dispose();
      });

      // dispose textures
      traverseMaterials(this.content, function (material) {
        MAP_NAMES.forEach(function (map) {
          if (material[map]) material[map].dispose();
        });
      });
    }
  }]);

  return Viewer;
}();

function traverseMaterials(object, callback) {
  object.traverse(function (node) {
    if (!node.isMesh) return;
    var materials = Array.isArray(node.material) ? node.material : [node.material];
    materials.forEach(callback);
  });
}

module.exports = function () {
  /**
   * @param  {Element} el
   * @param  {Location} location
   */
  function App(el, options, THREE, dat, Stats, xhr) {
    _classCallCheck(this, App);

    this.THREE = THREE;
    this.xhr = xhr;
    this.dat = dat;
    this.Stats = Stats;
    this.options = {
      kiosk: Boolean(options.kiosk),
      model: options.model || "",
      preset: options.preset || "",
      cameraPosition: options.cameraPosition ? options.cameraPosition.split(",").map(Number) : null
    };

    this.el = el;
    this.viewer = null;
    this.viewerEl = null;
    this.spinnerEl = el.querySelector(".spinner");
    this.dropEl = el.querySelector(".dropzone");
    this.inputEl = el.querySelector("#file-input");

    this.hideSpinner();

    this.options;

    if (this.options.kiosk) {
      var headerEl = document.querySelector("header");
      headerEl.style.display = "none";
    }

    if (this.options.model) {
      this.view(this.options.model, "", new Map());
    }
  }

  /**
   * Sets up the view manager.
   * @return {Viewer}
   */


  _createClass(App, [{
    key: "createViewer",
    value: function createViewer() {
      this.viewerEl = document.createElement("div");
      this.viewerEl.classList.add("viewer");
      this.dropEl.innerHTML = "";
      this.dropEl.appendChild(this.viewerEl);
      this.viewer = new Viewer(this.viewerEl, this.options, this.THREE, this.dat, this.Stats, this.xhr);
      return this.viewer;
    }

    /**
     * Loads a fileset provided by user action.
     * @param  {Map<string, File>} fileMap
     */

  }, {
    key: "load",
    value: function load(fileMap) {
      var rootFile = void 0;
      var rootPath = void 0;
      Array.from(fileMap).forEach(function (_ref) {
        var _ref2 = _slicedToArray(_ref, 2),
            path = _ref2[0],
            file = _ref2[1];

        if (file.name.match(/\.(gltf|glb)$/)) {
          rootFile = file;
          rootPath = path.replace(file.name, "");
        }
      });

      if (!rootFile) {
        this.onError("No .gltf or .glb asset found.");
      }

      this.view(rootFile, rootPath, fileMap);
    }

    /**
     * Passes a model to the viewer, given file and resources.
     * @param  {File|string} rootFile
     * @param  {string} rootPath
     * @param  {Map<string, File>} fileMap
     */

  }, {
    key: "view",
    value: function view(rootFile, rootPath, fileMap) {
      var _this10 = this;

      if (this.viewer) this.viewer.clear();

      var viewer = this.viewer || this.createViewer();

      var fileURL = typeof rootFile === "string" ? rootFile : URL.createObjectURL(rootFile);

      viewer.load(fileURL, rootPath, fileMap).catch(function (e) {
        return _this10.onError(e);
      }).then(function cleanup() {
        _this10.hideSpinner();
        if ((typeof rootFile === "undefined" ? "undefined" : _typeof(rootFile)) === "object") URL.revokeObjectURL(fileURL);
      });
    }
  }, {
    key: "clear",
    value: function clear() {
      if (this.viewer) this.viewer.clear();
    }

    /**
     * @param  {Error} error
     */

  }, {
    key: "onError",
    value: function onError(error) {
      if (error && error.target && error.target instanceof Image) {
        error = "Missing texture: " + error.target.src.split("/").pop();
      }
      window.alert((error || {}).message || error);
      console.error(error);
    }
  }, {
    key: "showSpinner",
    value: function showSpinner() {
      this.spinnerEl.style.display = "";
    }
  }, {
    key: "hideSpinner",
    value: function hideSpinner() {
      this.spinnerEl.style.display = "none";
    }
  }]);

  return App;
}();