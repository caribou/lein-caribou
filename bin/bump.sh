#!/bin/sh

# bump.sh - bump the version of the specified caribou subproject, and
# all projects which depend on that project, and publish
PACKAGE_NAME=$1

case $PACKAGE_NAME in
    core)
	UPDATE_CORE=true
	UPDATE_FRONTEND=true
	UPDATE_API=true
	UPDATE_ADMIN=true
	UPDATE_LEIN=true
	UPDATE_DEVELOPMENT=true;;
    development)
	UPDATE_CORE=false
	UPDATE_FRONTEND=false
	UPDATE_API=false
	UPDATE_ADMIN=false
	UPDATE_LEIN=false
	UPDATE_DEVELOPMENT=true;;
    lein)
	UPDATE_CORE=false
	UPDATE_FRONTEND=false
	UPDATE_API=false
	UPDATE_ADMIN=false
	UPDATE_LEIN=true
	UPDATE_DEVELOPMENT=true;;
    admin)
	UPDATE_CORE=false
	UPDATE_FRONTEND=false
	UPDATE_API=false
	UPDATE_ADMIN=true
	UPDATE_LEIN=false
	UPDATE_DEVELOPMENT=true;;
    api)
	UPDATE_CORE=false
	UPDATE_FRONTEND=false
	UPDATE_API=true
	UPDATE_ADMIN=false
	UPDATE_LEIN=false
	UPDATE_DEVELOPMENT=true;;
    frontend)
	UPDATE_CORE=false
	UPDATE_FRONTEND=true
	UPDATE_API=false
	UPDATE_ADMIN=false
	UPDATE_LEIN=false
	UPDATE_DEVELOPMENT=true;;

    *)
	echo "no such package" $1
	exit 1;;
esac

pull()
{
    PPATH=$1
# change the rest of the lines in this function to echo / do
    set -x
    cd ${PPATH} &&
    git checkout master &&
    git pull origin master &&
    set +x
}

update()
{
    PPATH=$1
    pull $PPATH
    if [ $? != 0 ] ;
    then
	echo "pull failed in" $PPATH
	exit 2
    fi
}

publish()
{
    PPATH=$1
# change the rest of the lines in this function to echo / do
    set -x
    cd ${PPATH} &&
    lein compile &&
# this is automated
#    lein push &&
    lein install &&
    git commit -a -m '"version bump"' &&
    git push
    set +x
}

commit()
{
    PPATH=$1
    publish $PPATH
    if [ $? != 0 ] ;
    then
	echo "push failed in" $PPATH
	exit 3
    fi
}

# this regex works with bsd sed
package_re() {
  PACKAGE=$1
  PRE=".*${PACKAGE}"
# regex fails with bsd sed
#     SPACES='[ \t]\+"'
  SPACES='[ \t][ \t]*"'
# regex fails with bsd sed
#     NUM='[0-9]\+'
  NUM='[0-9][0-9]*'
  SUF='".*$'
  echo "\(${PRE}\)${SPACES}\(${NUM}\.${NUM}\.\)\(${NUM}\)\(${SUF}\)"
}

package_minor()
{
    PACKAGE=$1
    FILE=$2
    RE="$(package_re $PACKAGE)"
    sed -n -e "s,$RE,\3,p" $FILE
}

bump()
{
    PACKAGE=$1
    PPATH=$2
    FILE=${PPATH}/project.clj
    RE=$(package_re ${PACKAGE})
    BUMPED=$3
    # build the regular expression
    echo in ${PPATH} ${PACKAGE}, upping to ${BUMPED}
  # change the rest of the lines in this function to echo / do
    NAME=$(mktemp /tmp/bump.XXXXXXX)
    sed -e "s,${RE},\1 \"\2${BUMPED}\4," < ${FILE} > ${NAME}
    if test -s ${NAME}
    then
	mv ${NAME} ${FILE}
    else
	echo ERROR ${FILE} generated empty new version
	rm ${NAME}
	exit 5
    fi
}

if $UPDATE_CORE ; then update ../caribou-core; fi
if $UPDATE_FRONTEND; then update ../caribou-frontend; fi
if $UPDATE_API; then update ../caribou-api; fi
if $UPDATE_ADMIN; then update ../caribou-admin; fi
if $UPDATE_LEIN; then update ../lein-caribou; fi
if $UPDATE_DEVELOPMENT ; then update ../caribou-development; fi

if $UPDATE_CORE ;
then
    VER=$(package_minor antler/caribou-core ../caribou-core/project.clj)
    if [ "${VER}" = "" ] ;
    then
	echo ERROR empty version for antler/caribou-core;
	exit 4
    fi
    BUMPED=$(expr ${VER} + 1)
    bump antler/caribou-core ../caribou-core ${BUMPED}
    bump antler/caribou-core ../caribou-frontend ${BUMPED}
    bump antler/caribou-core ../caribou-api ${BUMPED}
    bump antler/caribou-core ../lein-caribou ${BUMPED}
fi

if $UPDATE_FRONTEND ;
then
    VER=$(package_minor antler/caribou-frontend ../caribou-frontend/project.clj)
    if [ "${VER}" = "" ] ;
    then
	echo ERROR empty version for antler/caribou-frontend;
	exit 4
    fi
    BUMPED=$(expr ${VER} + 1)
    bump antler/caribou-frontend ../caribou-frontend ${BUMPED}
    bump antler/caribou-frontend ../caribou-development/site ${BUMPED}
fi

if $UPDATE_API ;
then
    VER=$(package_minor antler/caribou-api ../caribou-api/project.clj)
    if [ "${VER}" = "" ] ;
    then
	echo ERROR empty version for antler/caribou-api;
	exit 4
    fi
    BUMPED=$(expr ${VER} + 1)
    bump antler/caribou-api ../caribou-api ${BUMPED}
    bump antler/caribou-api ../caribou-development/api ${BUMPED}
fi

if $UPDATE_ADMIN ;
then
    VER=$(package_minor antler/caribou-admin ../caribou-admin/project.clj)
    if [ "${VER}" = "" ] ;
    then
	echo ERROR empty version for antler/caribou-admin;
	exit 4
    fi
    BUMPED=$(expr ${VER} + 1)
    bump antler/caribou-admin ../caribou-admin ${BUMPED}
    bump antler/caribou-admin ../caribou-development/api ${BUMPED}
fi
	
if $UPDATE_LEIN ;
then
    VER=$(package_minor antler/lein-caribou ../lein-caribou/project.clj)
    if [ "${VER}" = "" ] ;
    then
	echo ERROR empty version for antler/lein-caribou;
	exit 4
    fi
    BUMPED=$(expr ${VER} + 1)
    bump antler/lein-caribou ../lein-caribou ${BUMPED}
    bump antler/lein-caribou ../caribou-development ${BUMPED}
fi

if $UPDATE_DEVELOPMENT ;
then
    VER=$(package_minor antler/caribou-development \
	../caribou-development/project.clj)
    bump antler/caribou-development ../caribou-development $(expr ${VER} + 1)
fi

if $UPDATE_CORE ; then commit ../caribou-core; fi
if $UPDATE_FRONTEND; then commit ../caribou-frontend; fi
if $UPDATE_API; then commit ../caribou-api; fi
if $UPDATE_ADMIN; then commit ../caribou-admin; fi
if $UPDATE_LEIN; then commit ../lein-caribou; fi
if $UPDATE_DEVELOPMENT ; then commit ../caribou-development; fi
