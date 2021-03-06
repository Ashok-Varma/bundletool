/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.tools.build.bundletool.model;

import static com.android.tools.build.bundletool.testing.ManifestProtoUtils.androidManifest;
import static com.android.tools.build.bundletool.testing.ManifestProtoUtils.withEmptyDeliveryElement;
import static com.android.tools.build.bundletool.testing.ManifestProtoUtils.withFeatureCondition;
import static com.android.tools.build.bundletool.testing.ManifestProtoUtils.withFeatureConditionHexVersion;
import static com.android.tools.build.bundletool.testing.ManifestProtoUtils.withFusingAttribute;
import static com.android.tools.build.bundletool.testing.ManifestProtoUtils.withInstallTimeDelivery;
import static com.android.tools.build.bundletool.testing.ManifestProtoUtils.withMinSdkCondition;
import static com.android.tools.build.bundletool.testing.ManifestProtoUtils.withMinSdkVersion;
import static com.android.tools.build.bundletool.testing.ManifestProtoUtils.withOnDemandDelivery;
import static com.android.tools.build.bundletool.testing.ManifestProtoUtils.withUnsupportedCondition;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.android.aapt.Resources.XmlNode;
import com.android.tools.build.bundletool.exceptions.ValidationException;
import com.android.tools.build.bundletool.utils.xmlproto.XmlProtoAttributeBuilder;
import com.android.tools.build.bundletool.utils.xmlproto.XmlProtoElement;
import com.android.tools.build.bundletool.utils.xmlproto.XmlProtoElementBuilder;
import com.android.tools.build.bundletool.utils.xmlproto.XmlProtoNode;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ManifestDeliveryElementTest {

  private static final String DISTRIBUTION_NAMESPACE_URI =
      "http://schemas.android.com/apk/distribution";

  @Test
  public void emptyDeliveryElement_notWellFormed() {
    Optional<ManifestDeliveryElement> deliveryElement =
        ManifestDeliveryElement.fromManifestRootNode(
            androidManifest("com.test.app", withEmptyDeliveryElement()));

    assertThat(deliveryElement).isPresent();
    assertThat(deliveryElement.get().hasInstallTimeElement()).isFalse();
    assertThat(deliveryElement.get().hasOnDemandElement()).isFalse();
    assertThat(deliveryElement.get().hasModuleConditions()).isFalse();
    assertThat(deliveryElement.get().isWellFormed()).isFalse();
  }

  @Test
  public void installTimeDeliveryOnly() {
    Optional<ManifestDeliveryElement> deliveryElement =
        ManifestDeliveryElement.fromManifestRootNode(
            androidManifest("com.test.app", withInstallTimeDelivery()));

    assertThat(deliveryElement).isPresent();
    assertThat(deliveryElement.get().hasInstallTimeElement()).isTrue();
    assertThat(deliveryElement.get().hasOnDemandElement()).isFalse();
    assertThat(deliveryElement.get().hasModuleConditions()).isFalse();
    assertThat(deliveryElement.get().isWellFormed()).isTrue();
  }

  @Test
  public void onDemandDeliveryOnly() {
    Optional<ManifestDeliveryElement> deliveryElement =
        ManifestDeliveryElement.fromManifestRootNode(
            androidManifest("com.test.app", withOnDemandDelivery()));

    assertThat(deliveryElement).isPresent();
    assertThat(deliveryElement.get().hasInstallTimeElement()).isFalse();
    assertThat(deliveryElement.get().hasOnDemandElement()).isTrue();
    assertThat(deliveryElement.get().hasModuleConditions()).isFalse();
    assertThat(deliveryElement.get().isWellFormed()).isTrue();
  }

  @Test
  public void onDemandAndInstallTimeDelivery() {
    Optional<ManifestDeliveryElement> deliveryElement =
        ManifestDeliveryElement.fromManifestRootNode(
            androidManifest("com.test.app", withInstallTimeDelivery(), withOnDemandDelivery()));

    assertThat(deliveryElement).isPresent();
    assertThat(deliveryElement.get().hasInstallTimeElement()).isTrue();
    assertThat(deliveryElement.get().hasOnDemandElement()).isTrue();
    assertThat(deliveryElement.get().hasModuleConditions()).isFalse();
    assertThat(deliveryElement.get().isWellFormed()).isTrue();
  }

  @Test
  public void getModuleConditions_returnsAllConditions() {
    Optional<ManifestDeliveryElement> deliveryElement =
        ManifestDeliveryElement.fromManifestRootNode(
            androidManifest(
                "com.test.app",
                withFeatureCondition("android.hardware.camera.ar"),
                withMinSdkCondition(24)));

    assertThat(deliveryElement).isPresent();

    assertThat(deliveryElement.get().hasModuleConditions()).isTrue();
    assertThat(deliveryElement.get().getModuleConditions())
        .isEqualTo(
            ModuleConditions.builder()
                .addDeviceFeatureCondition(
                    DeviceFeatureCondition.create("android.hardware.camera.ar"))
                .setMinSdkVersion(24)
                .build());
  }

  @Test
  public void getDeviceFeatureConditions_returnsAllConditions() {
    Optional<ManifestDeliveryElement> deliveryElement =
        ManifestDeliveryElement.fromManifestRootNode(
            androidManifest(
                "com.test.app",
                withFeatureCondition("android.hardware.camera.ar"),
                withFeatureCondition("android.software.vr.mode"),
                withMinSdkVersion(24)));

    assertThat(deliveryElement).isPresent();

    assertThat(deliveryElement.get().hasModuleConditions()).isTrue();
    assertThat(deliveryElement.get().getModuleConditions().getDeviceFeatureConditions())
        .containsExactly(
            DeviceFeatureCondition.create("android.hardware.camera.ar"),
            DeviceFeatureCondition.create("android.software.vr.mode"));
  }

  @Test
  public void moduleConditions_deviceFeatureVersions() {
    Optional<ManifestDeliveryElement> deliveryElement =
        ManifestDeliveryElement.fromManifestRootNode(
            androidManifest(
                "com.test.app",
                withFeatureConditionHexVersion("android.software.opengl", 0x30000),
                withFeatureCondition("android.hardware.vr.headtracking", 1)));

    assertThat(deliveryElement).isPresent();

    assertThat(deliveryElement.get().hasModuleConditions()).isTrue();
    assertThat(deliveryElement.get().getModuleConditions().getDeviceFeatureConditions())
        .containsExactly(
            DeviceFeatureCondition.create("android.software.opengl", Optional.of(0x30000)),
            DeviceFeatureCondition.create("android.hardware.vr.headtracking", Optional.of(1)));
  }

  @Test
  public void moduleConditions_unsupportedCondition_throws() throws Exception {
    Optional<ManifestDeliveryElement> manifestDeliveryElement =
        ManifestDeliveryElement.fromManifestRootNode(
            androidManifest(
                "com.test.app", withFusingAttribute(false), withUnsupportedCondition()));

    assertThat(manifestDeliveryElement).isPresent();

    Throwable exception =
        assertThrows(
            ValidationException.class, () -> manifestDeliveryElement.get().getModuleConditions());
    assertThat(exception)
        .hasMessageThat()
        .contains("Unrecognized module condition: 'unsupportedCondition'");
  }

  @Test
  public void moduleConditions_missingNameOfFeature_throws() throws Exception {
    // Name attribute doesn't use distribution namespace.
    XmlProtoElement badCondition =
        XmlProtoElementBuilder.create(DISTRIBUTION_NAMESPACE_URI, "device-feature")
            .addAttribute(
                XmlProtoAttributeBuilder.create("name")
                    .setValueAsString("android.hardware.camera.ar"))
            .build();

    Optional<ManifestDeliveryElement> manifestDeliveryElement =
        ManifestDeliveryElement.fromManifestRootNode(
            createAndroidManifestWithConditions(badCondition));

    assertThat(manifestDeliveryElement).isPresent();

    Throwable exception =
        assertThrows(
            ValidationException.class, () -> manifestDeliveryElement.get().getModuleConditions());
    assertThat(exception)
        .hasMessageThat()
        .contains("Missing required 'name' attribute in the 'device-feature' condition element.");
  }

  @Test
  public void moduleConditions_missingMinSdkValue_throws() {
    // Value attribute doesn't use distribution namespace.
    XmlProtoElement badCondition =
        XmlProtoElementBuilder.create(DISTRIBUTION_NAMESPACE_URI, "min-sdk")
            .addAttribute(XmlProtoAttributeBuilder.create("value").setValueAsDecimalInteger(26))
            .build();

    Optional<ManifestDeliveryElement> manifestDeliveryElement =
        ManifestDeliveryElement.fromManifestRootNode(
            createAndroidManifestWithConditions(badCondition));

    assertThat(manifestDeliveryElement).isPresent();

    Throwable exception =
        assertThrows(
            ValidationException.class, () -> manifestDeliveryElement.get().getModuleConditions());
    assertThat(exception)
        .hasMessageThat()
        .contains("Missing required 'value' attribute in the 'min-sdk' condition element.");
  }

  private static XmlNode createAndroidManifestWithConditions(XmlProtoElement... conditions) {
    XmlProtoElementBuilder conditionsBuilder =
        XmlProtoElementBuilder.create(DISTRIBUTION_NAMESPACE_URI, "conditions");
    for (XmlProtoElement condition : conditions) {
      conditionsBuilder.addChildElement(condition.toBuilder());
    }

    return XmlProtoNode.createElementNode(
            XmlProtoElementBuilder.create("manifest")
                .addChildElement(
                    XmlProtoElementBuilder.create(DISTRIBUTION_NAMESPACE_URI, "module")
                        .addChildElement(
                            XmlProtoElementBuilder.create(DISTRIBUTION_NAMESPACE_URI, "delivery")
                                .addChildElement(
                                    XmlProtoElementBuilder.create(
                                            DISTRIBUTION_NAMESPACE_URI, "install-time")
                                        .addChildElement(conditionsBuilder))))
                .build())
        .getProto();
  }
}
