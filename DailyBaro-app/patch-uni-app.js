import fs from 'fs';
const path = './node_modules/@dcloudio/uni-app/dist/uni-app.es.js';

let content = fs.readFileSync(path, 'utf8');

// 替换第一行的导入，移除不存在的导出
content = content.replace(
  "import { shallowRef, ref, getCurrentInstance, isInSSRComponentSetup, injectHook } from 'vue';",
  `import { shallowRef, ref, getCurrentInstance } from 'vue';
const isInSSRComponentSetup = false;
const injectHook = (lifecycle, hook, target) => {
  if (target) {
    const hooks = target[lifecycle] || (target[lifecycle] = []);
    hooks.push(hook);
  }
};`
);

fs.writeFileSync(path, content);
console.log('✅ 已修复 uni-app.es.js');
