import * as React from 'react';

import { StyleSheet, View, NativeModules, Button } from 'react-native';
import { multiply } from 'react-native-scan-kit';

const ScanKit = NativeModules.ScanKit;

export default function App() {
  return (
    <View style={styles.container}>
      <Button
        onPress={() => {
          // console.log('ScanKit', ScanKit);
          ScanKit.scan();
        }}
        title="扫码"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
