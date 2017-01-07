export VERSION_SDK_TOOLS="24.0.2"
export ENV VERSION_BUILD_TOOLS="23.0.2"
export VERSION_TARGET_SDK="23"

export SDK_PACKAGES="build-tools-${VERSION_BUILD_TOOLS},android-${VERSION_TARGET_SDK},addon-google_apis-google-${VERSION_TARGET_SDK},platform-tools,extra-android-m2repository,extra-android-support,extra-google-google_play_services,extra-google-m2repository"

export ANDROID_HOME="/sdk"
export PATH="$PATH:${ANDROID_HOME}/tools"
export DEBIAN_FRONTEND=noninteractive

sudo apt-get -qq update && \
    sudo apt-get install -qqy --no-install-recommends \
      curl \
      html2text \
      openjdk-8-jdk \
      libc6-i386 \
      lib32stdc++6 \
      lib32gcc1 \
      lib32ncurses5 \
      lib32z1 \
      unzip \
    && sudo rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

sudo rm -f /etc/ssl/certs/java/cacerts; \
    /var/lib/dpkg/info/ca-certificates-java.postinst configure

curl -s http://dl.google.com/android/repository/tools_r${VERSION_SDK_TOOLS}-linux.zip > /tools.zip && \
    unzip /tools.zip -d /sdk && \
    rm -v /tools.zip

sudo mkdir -p $ANDROID_HOME/licenses/ \
  && echo "8933bad161af4178b1185d1a37fbf41ea5269c55" > $ANDROID_HOME/licenses/android-sdk-license \
  && echo "84831b9409646a918e30573bab4c9c91346d8abd" > $ANDROID_HOME/licenses/android-sdk-preview-license

(while [ 1 ]; do sleep 5; echo y; done) | ${ANDROID_HOME}/tools/android update sdk -u -a -t ${SDK_PACKAGES}