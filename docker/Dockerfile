FROM ubuntu:20.04
RUN apt-get update && apt-get upgrade -y \
    && apt-get install wget -y \
    && apt-get install ssh -y \
    && apt-get install openjdk-8-jdk -y \
    && apt-get install openssh-server \
    && apt-get install sudo -y \
    && apt-get install nano -y

COPY sshd_config /etc/ssh/sshd_config

# Add user
RUN useradd -m hadoopuser && echo "hadoopuser:supergroup" | chpasswd && adduser hadoopuser sudo && echo "hadoopuser     ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers && cd /usr/bin/ && sudo ln -s python3 python
USER hadoopuser
WORKDIR /home/hadoopuser
# SSH config
RUN ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa && cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && chmod 0600 ~/.ssh/authorized_keys

# Setup hadoop
# RUN wget -c -O hadoop-2.7.7.tar.gz https://archive.apache.org/dist/hadoop/common/hadoop-2.7.7/hadoop-2.7.7.tar.gz
RUN tar zxvf hadoop-2.7.7.tar.gz && rm hadoop-2.7.7.tar.gz
RUN mv hadoop-2.7.7 hadoop

COPY core-site.xml $HADOOP_HOME/etc/hadoop/
COPY hdfs-site.xml $HADOOP_HOME/etc/hadoop/
COPY yarn-site.xml $HADOOP_HOME/etc/hadoop/
COPY mapred-site.xml $HADOOP_HOME/etc/hadoop/

# Prepare environment
ENV HADOOP_HOME /home/hadoopuser/hadoop
RUN echo "export JAVA_HOME=/usr" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh

RUN echo "export HADOOP_HOME=/home/hadoopuser/hadoop" >> /home/hadoopuser/.bashrc
RUN echo "export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre/" >> /home/hadoopuser/.bashrc
RUN echo "export PATH=\$PATH:\$HADOOP_HOME/bin" >> /home/hadoopuser/.bashrc
RUN echo "export PATH=\$PATH:\$HADOOP_HOME/sbin" >> /home/hadoopuser/.bashrc
RUN echo "export HADOOP_MAPRED_HOME=\$HADOOP_HOME" >> /home/hadoopuser/.bashrc
RUN echo "export HADOOP_COMMON_HOME=\$HADOOP_HOME" >> /home/hadoopuser/.bashrc
RUN echo "export HADOOP_HDFS_HOME=\$HADOOP_HOME" >> /home/hadoopuser/.bashrc
RUN echo "export YARN_HOME=\$HADOOP_HOME" >> /home/hadoopuser/.bashrc
RUN echo "export HADOOP_COMMON_LIB_NATIVE_DIR=\$HADOOP_HOME/lib/native" >> /home/hadoopuser/.bashrc
RUN echo 'export HADOOP_OPTS="-Djava.library.path=\$HADOOP_HOME/lib"' >> /home/hadoopuser/.bashrc

# Copy setup files
COPY core-site.xml $HADOOP_HOME/etc/hadoop/
COPY hdfs-site.xml $HADOOP_HOME/etc/hadoop/
COPY yarn-site.xml $HADOOP_HOME/etc/hadoop/
COPY mapred-site.xml $HADOOP_HOME/etc/hadoop/


EXPOSE 54310 54311 50070 50075 50010 50020 50090 8020 9000 9864 9870 10020 19888 8088 8030 8031 8032 8033 8040 8042 22