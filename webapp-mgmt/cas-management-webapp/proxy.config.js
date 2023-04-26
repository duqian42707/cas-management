const proxy = {
  target: "http://localhost:8080",
  changeOrigin: true,
  secure: false
}
// const prefix = "";
const prefix = "/backstage/cas-management";
const apis = [
  '/appConfig',
  '/user',
  '/footer',
  '/images',
  '/gitStatus',
  '/getService',
  '/getServices',
  '/formData',
  '/saveService',
  '/getYaml',
  '/getJson',
  '/updateOrder',
  '/deleteRegisteredService',
  '/search',
]

const data = {};

for (let i = 0; i < apis.length; i++) {
  const path = prefix + apis[i]
  data[path] = proxy;
}

module.exports = data;
