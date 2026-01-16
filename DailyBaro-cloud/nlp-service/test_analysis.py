#!/usr/bin/env python3
import sys
print("Python版本:", sys.version)
print("开始测试...")

try:
    import pandas as pd
    print("✓ pandas已安装")
except ImportError as e:
    print("✗ pandas未安装:", e)
    sys.exit(1)

try:
    import matplotlib
    print("✓ matplotlib已安装")
except ImportError as e:
    print("✗ matplotlib未安装:", e)

try:
    import seaborn
    print("✓ seaborn已安装")
except ImportError as e:
    print("✗ seaborn未安装:", e)

print("测试完成")
